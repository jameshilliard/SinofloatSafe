package com.sinofloat.mobilesafe.widget.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.graphics.PointF;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

abstract class RendererGLES20 extends GLES20
{
	protected int _program;

	protected boolean buildProgram(String vertShaderSource, String fragShaderSource) {
                int vertShader = -1;
		int fragShader = -1;

		// create shader program
                _program = glCreateProgram();
                
                // create and compile vertex shader
		vertShader = compileShader(GL_VERTEX_SHADER, vertShaderSource);
                if (-1 == vertShader) {
			destroyShaders(vertShader, fragShader, _program);
			Log.v(TAG, "vert shader fail.");
                        return false;
                }
                
                // create and compile fragment shader
		fragShader = compileShader(GL_FRAGMENT_SHADER, fragShaderSource);
                if (-1 == fragShader) {
                        destroyShaders(vertShader, fragShader, _program);
			Log.v(TAG, "frag shader fail.");
                        return false;
                }

                // attach vertex shader to program
                glAttachShader(_program, vertShader);
                // attach fragment shader to program
                glAttachShader(_program, fragShader);
                
                // link program
		if (!linkProgram(_program)) {
			destroyShaders(vertShader, fragShader, _program);
                        return false;
		}

		glDeleteShader(vertShader);
		glDeleteShader(fragShader);
                
                return true;
        }

	protected static FloatBuffer fBuffer(float[]a) {
		// 先初始化buffer,数组的长度*4,因为一个float占4个字节  
		ByteBuffer mbb=ByteBuffer.allocateDirect(a.length*4);  
		// 数组排列用nativeOrder  
		mbb.order(ByteOrder.nativeOrder());  
		FloatBuffer floatBuffer=mbb.asFloatBuffer();  
		floatBuffer.put(a);  
		floatBuffer.position(0);  
		return floatBuffer;  
	}

	protected static int nextPOT(int x) {
		x = x - 1;
		x = x | (x >> 1);
		x = x | (x >> 2);
		x = x | (x >> 4);
		x = x | (x >> 8);
		x = x | (x >>16);
		return x + 1;
	}

	/* Create and compile a shader from the provided source(s) */
	private static int compileShader(int type, String sources) {
		int shader = glCreateShader(type);
		glShaderSource(shader, sources);	// set source code in the shader
		glCompileShader(shader);		// compile shader

		IntBuffer intBuffer = IntBuffer.allocate(1);
		glGetShaderiv(shader, GL_COMPILE_STATUS, intBuffer);
		if (intBuffer.get(0) == GL_FALSE) {
			Log.v(TAG, "Failed to compile shader.");
			glDeleteShader(shader);
			return -1;
		}

		return shader;
	}

	private static boolean linkProgram(int program) {
		glLinkProgram(program);

		IntBuffer intBuffer = IntBuffer.allocate(1);
		glGetProgramiv(program, GL_LINK_STATUS, intBuffer);
		if (intBuffer.get(0) == GL_FALSE) {
			Log.v(TAG, "Failed to link program.");
			return false;
		}
        
		return true;
	}

	/* delete shader resources */
	private static void destroyShaders(int vertShader, int fragShader, int program) {
		if (-1 != vertShader) {
			glDeleteShader(vertShader);
		}

		if (-1 != fragShader) {
			glDeleteShader(fragShader);
		}

		if (-1 != program) {
			glDeleteProgram(program);
		}
	}

	private static final String TAG = "RendererGLES20";
}

class VideoRendererGLES20 extends RendererGLES20
{
	public void setStretchToFit(boolean isStretchToFit) {
		_isStretchToFit = isStretchToFit;
	}

	public void scaleAndMoveViewPort(float scaleInc, PointF origFocalPoint, 
					 float distanceX, float distanceY) {
		float newScale = _scale + scaleInc;
		newScale = (1.0f > newScale) ? 1.0f : newScale;

		//Log.v(TAG, "distanceX: " + distanceX + ", distanceY: " + distanceY);
		//Log.v(TAG, "scaleInc: " + scaleInc);
		
		_viewPortX = ((_viewPortX - origFocalPoint.x) / _scale) * newScale + origFocalPoint.x;
		_viewPortY = ((_viewPortY - origFocalPoint.y) / _scale) * newScale + origFocalPoint.y;

		_scale = newScale;

		moveViewPort(distanceX, distanceY);
	}

	public void moveViewPort(float distanceX, float distanceY) {
		float minViewPortX = - DisplayView.backingWidth() * (_scale - 1.0f);
		float minViewPortY = - DisplayView.backingHeight() * (_scale - 1.0f);

		_viewPortX += distanceX;
		if (0.0f < _viewPortX) {
			_viewPortX = 0.0f;
		} else if (minViewPortX > _viewPortX) {
			_viewPortX = minViewPortX;
		}

		_viewPortY += distanceY;
		if (0.0f < _viewPortY) {
			_viewPortY = 0.0f;
		} else if (minViewPortY > _viewPortY) {
			_viewPortY = minViewPortY;
		}

		//Log.v(TAG, "_viewPortX: " + _viewPortX + ", _viewPortY: " + _viewPortY);
	}

	public void restoreViewPort() {
		_scale = 1.0f;
		_viewPortX = 0.0f;
		_viewPortY = 0.0f;
	}

	// NOTE: use duplicate method of ByteBuffer is more effective
	public synchronized void updateVideo(ByteBuffer videoBufferY, ByteBuffer videoBufferU, ByteBuffer videoBufferV, 
					     int videoWidth, int videoHeight, int orientation) {
		if (_videoWidth != videoWidth ||
		    _videoHeight != videoHeight) {
			
			_videoBuffer = null;
			_videoBufferY = null;
			_videoBufferU = null;
			_videoBufferV = null;

			_videoBufferY = ByteBuffer.allocate(videoWidth * videoHeight);
			_videoBufferU = ByteBuffer.allocate(videoWidth * videoHeight / 4);
			_videoBufferV = ByteBuffer.allocate(videoWidth * videoHeight / 4);

			_videoWidth = videoWidth;
			_videoHeight = videoHeight;
		}

		_orientation = orientation;
		
		videoBufferY.position(0);
		_videoBufferY.position(0);
		_videoBufferY.put(videoBufferY);

		videoBufferU.position(0);
		_videoBufferU.position(0);
		_videoBufferU.put(videoBufferU);

		videoBufferV.position(0);
		_videoBufferV.position(0);
		_videoBufferV.put(videoBufferV);
	}
	
	public synchronized void updateVideo(byte[] videoBuffer, int videoWidth, int videoHeight, int orientation) {
		if (_videoWidth != videoWidth ||
		    _videoHeight != videoHeight) {
			
			_videoBuffer = null;
			_videoBufferY = null;
			_videoBufferU = null;
			_videoBufferV = null;

			//_videoBuffer = ByteBuffer.allocate(videoWidth * videoHeight * 3 / 2);
			_videoBufferY = ByteBuffer.allocate(videoWidth * videoHeight);
			_videoBufferU = ByteBuffer.allocate(videoWidth * videoHeight / 4);
			_videoBufferV = ByteBuffer.allocate(videoWidth * videoHeight / 4);

			_videoWidth = videoWidth;
			_videoHeight = videoHeight;
		}

		//_isRenderVideo = true;
		_orientation = orientation;

		/*
		  _videoBuffer.position(0);
		  _videoBuffer.put(videoBuffer);
		*/
		_videoBufferY.position(0);
		_videoBufferY.put(videoBuffer, 0, videoWidth * videoHeight);

		_videoBufferU.position(0);
		_videoBufferU.put(videoBuffer, videoWidth * videoHeight, videoWidth * videoHeight / 4);

		_videoBufferV.position(0);
		_videoBufferV.put(videoBuffer, videoWidth * videoHeight * 5 / 4, videoWidth * videoHeight / 4);
	}

	public void init() {
		if (!loadShaders()) {
			Log.v(TAG, "loadShaders fail");
                        return;
                }

		IntBuffer intBuffer = IntBuffer.allocate(TEX_NUM);
                glGenTextures(TEX_NUM, intBuffer);
		for (int i=0; i<TEX_NUM; i++) {
			_videoTexture[i] = intBuffer.get(i);
		}
	}

	/* Note that when the EGL context is lost, replay_picture_all OpenGL resources associated with that context
	   will be automatically deleted. You do not need to call the corresponding "glDelete" methods 
	   such as glDeleteTextures to manually delete these lost resources.

	   protected void finalize() {
	   if (0 != _videoTexture[0]) {
	   glDeleteTextures(TEX_NUM, IntBuffer.wrap(_videoTexture));
	   for (int i=0; i<TEX_NUM; i++) {
	   _videoTexture[i] = 0;
	   }
	   }
	   }
	*/

	private boolean isVideoResolutionChanged() {
		/*
		  return (_videoWidth != _videoTexWidth ||
		  _videoHeight != _videoTexHeight);
		*/

		return (nextPOT(_videoWidth) != _videoTexWidth ||
			nextPOT(_videoHeight) != _videoTexHeight);
	}

	private void resizeVideoTexture() {
		//int wrapParam = GL_REPEAT;
		// 解决视频边框异常的问题
		int wrapParam = GL_CLAMP_TO_EDGE;

		/*
		  _videoTexWidth = _videoWidth;
		  _videoTexHeight = _videoHeight;
		*/

		_videoTexWidth = nextPOT(_videoWidth);
		_videoTexHeight = nextPOT(_videoHeight);

		glActiveTexture(GL_TEXTURE0);
		glEnable(GL_TEXTURE_2D);
		glBindTexture(GL_TEXTURE_2D, _videoTexture[TEX_Y_IDX]);
		// Configure Texture Parameters Before Loading Texture Image Data
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapParam);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapParam);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, _videoTexWidth, _videoTexHeight, 0, 
			     GL_LUMINANCE, GL_UNSIGNED_BYTE, null);

		glActiveTexture(GL_TEXTURE1);
		glEnable(GL_TEXTURE_2D);
		glBindTexture(GL_TEXTURE_2D, _videoTexture[TEX_U_IDX]);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapParam);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapParam);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, _videoTexWidth/2, _videoTexHeight/2, 0, 
			     GL_LUMINANCE, GL_UNSIGNED_BYTE, null);

		glActiveTexture(GL_TEXTURE2);
		glEnable(GL_TEXTURE_2D);
		glBindTexture(GL_TEXTURE_2D, _videoTexture[TEX_V_IDX]);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapParam);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapParam);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, _videoTexWidth/2, _videoTexHeight/2, 0, 
			     GL_LUMINANCE, GL_UNSIGNED_BYTE, null);
	}

	private void updateVideoTexture() {
		/*
		  glActiveTexture(GL_TEXTURE0);
		  glEnable(GL_TEXTURE_2D);
		  glBindTexture(GL_TEXTURE_2D, _videoTexture[TEX_Y_IDX]);
		  glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, _videoWidth, _videoHeight, GL_LUMINANCE, GL_UNSIGNED_BYTE, 
		  _videoBuffer.position(0));

		  glActiveTexture(GL_TEXTURE1);
		  glEnable(GL_TEXTURE_2D);
		  glBindTexture(GL_TEXTURE_2D, _videoTexture[TEX_U_IDX]);
		  glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, _videoWidth/2, _videoHeight/2, GL_LUMINANCE, GL_UNSIGNED_BYTE, 
		  _videoBuffer.position(_videoWidth * _videoHeight));

		  glActiveTexture(GL_TEXTURE2);
		  glEnable(GL_TEXTURE_2D);
		  glBindTexture(GL_TEXTURE_2D, _videoTexture[TEX_V_IDX]);
		  glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, _videoWidth/2, _videoHeight/2, GL_LUMINANCE, GL_UNSIGNED_BYTE, 
		  _videoBuffer.position(_videoWidth * _videoHeight * 5 / 4));
		*/

		if(_videoBufferY == null || _videoBufferU == null || _videoBufferV == null){
			return;
		}
		
		glActiveTexture(GL_TEXTURE0);
		glEnable(GL_TEXTURE_2D);
		glBindTexture(GL_TEXTURE_2D, _videoTexture[TEX_Y_IDX]);
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, _videoWidth, _videoHeight, GL_LUMINANCE, GL_UNSIGNED_BYTE, 
				_videoBufferY.position(0));

		glActiveTexture(GL_TEXTURE1);
		glEnable(GL_TEXTURE_2D);
		glBindTexture(GL_TEXTURE_2D, _videoTexture[TEX_U_IDX]);
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, _videoWidth/2, _videoHeight/2, GL_LUMINANCE, GL_UNSIGNED_BYTE, 
				_videoBufferU.position(0));

		glActiveTexture(GL_TEXTURE2);
		glEnable(GL_TEXTURE_2D);
		glBindTexture(GL_TEXTURE_2D, _videoTexture[TEX_V_IDX]);
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, _videoWidth/2, _videoHeight/2, GL_LUMINANCE, GL_UNSIGNED_BYTE, 
				_videoBufferV.position(0));
	}

	public synchronized void render(Rect area) {
		if (isVideoResolutionChanged()) {
			resizeVideoTexture();
		}

		updateVideoTexture();

		renderVideo(area);
	}

        private void renderVideo(Rect area) {
		float[] projection = new float[16];
		float[] modelView = new float[16];
                float[] mvp = new float[16];
                
                // 这里指定屏幕上绘图区域的四个定点坐标
                float width = area.width();
                float height = area.height();

		glViewport(area.left, area.top, (int)width, (int)height);
		Matrix.orthoM(projection, 0, 0, width, 0, height, -1.0f, 1.0f);

		float[] vertices;

		if (_isStretchToFit) {
			float verticesFixedRatio[] = {
				0.0f, 0.0f,
				width, 0.0f,
				0.0f, height,
				width, height,
			};

			vertices = verticesFixedRatio;
		} else {
			//按视频的宽高比对图像数据进行拉伸
			float renderOriginX, renderOriginY;
			float renderWidth, renderHeight;
			float ratio = (float)_videoWidth / _videoHeight;
			if (width / ratio < height) {
				renderWidth = width;
				renderHeight = renderWidth / ratio;
				renderOriginX = 0.0f;
				renderOriginY = (height - renderHeight) / 2;
			} else {
				renderHeight = height;
				renderWidth = renderHeight * ratio;
				renderOriginX = (width - renderWidth) / 2;
				renderOriginY = 0.0f;
			}
                
			float verticesStretchToFit[] = {
				renderOriginX, renderOriginY,
				renderOriginX + renderWidth, renderOriginY,
				renderOriginX, renderOriginY + renderHeight,
				renderOriginX + renderWidth, renderOriginY + renderHeight,
			};

			vertices = verticesStretchToFit;
		}

                // 指定在屏幕绘图区域上使用的纹理尺寸
		// NOTE: _videoWidth - 1 可以解决视频边框异常的问题
                float ws = (float)(_videoWidth - 1) / _videoTexWidth;
                float hs = (float)(_videoHeight - 1) / _videoTexHeight;
                float texCoords[] = {
                        0.0f, 0.0f,
                        ws, 0.0f,
                        0.0f, hs,
                        ws, hs
                };

                // 对视口矩阵进行坐标变换，使图像能在正确的方向和位置上显示
                //glLoadIdentity();
                //mtxLoadIdentity(modelView);
		Matrix.setIdentityM(modelView, 0);

                // 翻转类型  1 上下翻转 2 左右翻转 3 上下左右翻转
                
                // 先恢复倒正常（orientation ＝ 0）的状态
                // 上下倒转（左右会调换）
                //glRotatef(180, 0, 0, 1);
                //mtxRotateApply(modelView, 180, 0, 0, 1);
		Matrix.rotateM(modelView, 0, 180, 0, 0, 1);
                //glTranslatef(-width, -height, 0);
                //mtxTranslateApply(modelView, -width, -height, 0);
		Matrix.translateM(modelView, 0, -width, -height, 0);
                // 左右翻转
                //glRotatef(180, 0, 1, 0);
                //mtxRotateApply(modelView, 180, 0, 1, 0);
		Matrix.rotateM(modelView, 0, 180, 0, 1, 0);
                //glTranslatef(-width, 0, 0);
                //mtxTranslateApply(modelView, -width, 0, 0);
		Matrix.translateM(modelView, 0, -width, 0, 0);
                
                switch (_orientation) {
		case 1:
			// 上下倒转（左右会调换）
			//glRotatef(180, 0, 0, 1);
			//mtxRotateApply(modelView, 180, 0, 0, 1);
			Matrix.rotateM(modelView, 0, 180, 0, 0, 1);
			//glTranslatef(-width, -height, 0);
			//mtxTranslateApply(modelView, -width, -height, 0);
			Matrix.translateM(modelView, 0, -width, -height, 0);
			// 左右翻转
			//glRotatef(180, 0, 1, 0);
			//mtxRotateApply(modelView, 180, 0, 1, 0);
			Matrix.rotateM(modelView, 0, 180, 0, 1, 0);
			//glTranslatef(-width, 0, 0);
			//mtxTranslateApply(modelView, -width, 0, 0);
			Matrix.translateM(modelView, 0, -width, 0, 0);
			break;
		case 2:
			// 左右翻转
			//glRotatef(180, 0, 1, 0);
			//mtxRotateApply(modelView, 180, 0, 1, 0);
			Matrix.rotateM(modelView, 0, 180, 0, 1, 0);
			//glTranslatef(-width, 0, 0);
			//mtxTranslateApply(modelView, -width, 0, 0);
			Matrix.translateM(modelView, 0, -width, 0, 0);
			break;
		case 3:
			// 上下倒转（左右会调换）
			//glRotatef(180, 0, 0, 1);
			//mtxRotateApply(modelView, 180, 0, 0, 1);
			Matrix.rotateM(modelView, 0, 180, 0, 0, 1);
			//glTranslatef(-width, -height, 0);
			//mtxTranslateApply(modelView, -width, -height, 0);
			Matrix.translateM(modelView, 0, -width, -height, 0);
			// 左右翻转：（已经在上下倒转时翻转过来了）
			break;
		case 0:
			// 不做任何处理
		default:
			break;
                }

		//--------------------------------------		
		// HANDLE VIDEO SCALING AND VIEW PORT MOVING HERE
		//--------------------------------------

		// NOTE: translate at first and then scale
		Matrix.translateM(modelView, 0, _viewPortX, _viewPortY, 0);
		Matrix.scaleM(modelView, 0, _scale, _scale, 0);

		//--------------------------------------
                
                //mtxMultiply(mvp, _projection, modelView);
		Matrix.multiplyMM(mvp, 0, projection, 0, modelView, 0);

                // Use the program that we previously created
                glUseProgram(_program);
                
                // Set the modelview projection matrix that we calculated above
                // in our vertex shader
                //glUniformMatrix4fv(_uniform[UNIFORM_IDX_MODELVIEW_PROJECTION_MATRIX], 1, GL_FALSE, mvp);
		glUniformMatrix4fv(_uniform[UNIFORM_IDX_MODELVIEW_PROJECTION_MATRIX], 1, false, mvp, 0);
                
                glEnableVertexAttribArray(_attrib[ATTRIB_IDX_POSITION]);
                glEnableVertexAttribArray(_attrib[ATTRIB_IDX_TEXCOORD]);
		/*
		  glVertexAttribPointer(ATTRIB_POSITION, 2, GL_FLOAT, 0, 0, vertices);
		  glVertexAttribPointer(ATTRIB_TEXCOORD, 2, GL_FLOAT, 0, 0, texCoords);
		*/
		glVertexAttribPointer(_attrib[ATTRIB_IDX_POSITION], 2, GL_FLOAT, false, 0, fBuffer(vertices));
		glVertexAttribPointer(_attrib[ATTRIB_IDX_TEXCOORD], 2, GL_FLOAT, false, 0, fBuffer(texCoords));
                
                glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        }

	private boolean loadShaders() {
		if (!buildProgram(videoVSH, videoFSH)) {
			return false;
		}
                
                // NOTE: this step is very important
                glUseProgram(_program);
                
                // get uniform locations
                _uniform[UNIFORM_IDX_MODELVIEW_PROJECTION_MATRIX] = glGetUniformLocation(_program, "modelViewProjectionMatrix");
                _uniform[UNIFORM_IDX_SAMPLER_Y] = glGetUniformLocation(_program, "samplerY");
                _uniform[UNIFORM_IDX_SAMPLER_U] = glGetUniformLocation(_program, "samplerU");
                _uniform[UNIFORM_IDX_SAMPLER_V] = glGetUniformLocation(_program, "samplerV");
                // 将Uniform变量与纹理单位绑定
                glUniform1i(_uniform[UNIFORM_IDX_SAMPLER_Y], 0);
                glUniform1i(_uniform[UNIFORM_IDX_SAMPLER_U], 1);
                glUniform1i(_uniform[UNIFORM_IDX_SAMPLER_V], 2);
                
		// get attrib locations
		_attrib[ATTRIB_IDX_POSITION] = glGetAttribLocation(_program, "inPosition");
		_attrib[ATTRIB_IDX_TEXCOORD] = glGetAttribLocation(_program, "inTexcoord");
                
                return true;
        }

	/*
	  enum {
	  ATTRIB_IDX_POSITION,
	  ATTRIB_IDX_TEXCOORD
	  };
	*/
	private static final int ATTRIB_IDX_POSITION = 0;
	private static final int ATTRIB_IDX_TEXCOORD = 1;
	private static final int ATTRIB_NUM = 2;
	private int[] _attrib = new int[ATTRIB_NUM];

	/*
	  enum {
	  UNIFORM_IDX_MODELVIEW_PROJECTION_MATRIX,
	  UNIFORM_IDX_SAMPLER_Y,
	  UNIFORM_IDX_SAMPLER_U,
	  UNIFORM_IDX_SAMPLER_V,
	  UNIFORM_NUM
	  };
	*/
	private static final int UNIFORM_IDX_MODELVIEW_PROJECTION_MATRIX = 0;
	private static final int UNIFORM_IDX_SAMPLER_Y = 1;
	private static final int UNIFORM_IDX_SAMPLER_U = 2;
	private static final int UNIFORM_IDX_SAMPLER_V = 3;
	private static final int UNIFORM_NUM = 4;
        //int _uniform[UNIFORM_NUM];
	private int[] _uniform = new int[UNIFORM_NUM];

	/*
	  enum {
	  TEX_Y_IDX,
	  TEX_U_IDX,
	  TEX_V_IDX,
	  TEX_NUM
	  };
	*/
	private static final int TEX_Y_IDX = 0;
	private static final int TEX_U_IDX = 1;
	private static final int TEX_V_IDX = 2;
	private static final int TEX_NUM = 3;
        //int _videoTexture[TEX_NUM];
	private int[] _videoTexture = new int[TEX_NUM];

        //private int _backingWidth;
        //private int _backingHeight;
        private int _videoWidth;
        private int _videoHeight;
        private int _videoTexWidth;
        private int _videoTexHeight;
        private int _orientation;

	//QGLShaderProgram _program;
        //QMatrix4x4 _projection;
	//private int _program;
        //private float[] _projection = new float[16];// 投影矩阵

	private ByteBuffer _videoBuffer;
	private ByteBuffer _videoBufferY;
	private ByteBuffer _videoBufferU;
	private ByteBuffer _videoBufferV;

	private float _scale = 1.0f;
	private float _viewPortX = 0.0f;// Modle view coord.
	private float _viewPortY = 0.0f;

	private boolean _isStretchToFit = false;

	private static final String videoVSH =
		"#ifdef GL_ES\n" +
		"precision highp float;\n" +
		"#endif\n" +
		"uniform mat4 modelViewProjectionMatrix;\n" +
		"#if __VERSION__ >= 140\n" +
		"in vec4 inPosition;\n" +
		"in vec2 inTexcoord;\n" +
		"out vec2 varTexcoord;\n" +
		"#else\n" +
		"attribute vec4 inPosition;\n" +
		"attribute vec2 inTexcoord;\n" +
		"varying vec2 varTexcoord;\n" +
		"#endif\n" +
		"void main(void) {\n" +
		"    gl_Position = modelViewProjectionMatrix * inPosition;\n" +
		"    varTexcoord = inTexcoord;\n" +
		"}\n";
	
	private static final String videoFSH =
		"#ifdef GL_ES\n" +
		"precision highp float;\n" +
		"#endif\n" +
		"#if __VERSION__ >= 140\n" +
		"in vec2 varTexcoord;\n" +
		"out vec4 fragColor;\n" +
		"#else\n" +
		"varying vec2 varTexcoord;\n" +
		"#endif\n" +
		"uniform sampler2D samplerY;\n" +
		"uniform sampler2D samplerU;\n" +
		"uniform sampler2D samplerV;\n" +
		"void main(void) {\n" +
		"#if 1\n" +
		"    mediump vec3 yuv;\n" +
		"    mediump vec3 rgb;\n" +
		"    mat3 yuv420pToRGB = mat3(1.0, 1.0, 1.0," +
		"                             0.0, -0.39465, 2.03211," +
		"                             1.13983, -0.58060, 0.0);\n" +
		"    yuv.x = texture2D(samplerY, varTexcoord).r;\n" +
		"    yuv.y = texture2D(samplerU, varTexcoord).r - 0.5;\n" +
		"    yuv.z = texture2D(samplerV, varTexcoord).r - 0.5;\n" +
		"    rgb = yuv420pToRGB * yuv;\n" +
		"#if __VERSION__ >= 140\n" +
		"    fragColor = vec4(rgb,1);\n" +
		"#else\n" +
		"    gl_FragColor = vec4(rgb,1);\n" +
		"#endif\n" +
		"#else\n" +
		"    gl_FragColor = vec4(texture2D(samplerY, varTexcoord).rgb, 1);\n" +
		"#endif\n" +
		"}\n";

	private static final String TAG = "VideoRendererGLES20";
}

class CommonRendererGLES20 extends RendererGLES20
{
	public void renderBlock(Rect area) {
		float width = area.width();
                float height = area.height();

		float vertices[] = {
                        0.0f, 0.0f,
                        width, 0.0f,
                        0.0f, height,
                        width, height,
                };
		
		float[] color = {0.0f, 0.0f, 0.0f, 1.0f};

		prepareRender(area, vertices, color);

                glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
	}
	
	public void render(Rect area, boolean isSelected) {
                float width = area.width();
                float height = area.height();

		float BORDER_WIDTH;
		float interval;
                
		float[] color = new float[4];
		if (isSelected) {
			/*
			color[0] = 0.0f;
			color[1] = 1.0f;
			color[2] = 1.0f;
			color[3] = 1.0f;
			*/
			color[0] = 0.0f;
			color[1] = 1.0f;
			color[2] = 0.0f;
			color[3] = 1.0f;

			BORDER_WIDTH = 2.0f;
			interval = BORDER_WIDTH / 2;
		} else {
			/*
			  color[0] = 0.6f;
			  color[1] = 0.6f;
			  color[2] = 0.6f;
			  color[3] = 1.0f;
			*/
			color[0] = 0.5f;
			color[1] = 0.5f;
			color[2] = 0.5f;
			color[3] = 1.0f;

			BORDER_WIDTH = 1.0f;
			interval = BORDER_WIDTH / 2;
		}

		float vertices[] = {
                        interval, interval,
                        width - interval, interval, 
                        width - interval, height - interval,
                        interval, height - interval
                };
		
		prepareRender(area, vertices, color);
		
		glLineWidth(BORDER_WIDTH);
                glDrawArrays(GL_LINE_LOOP, 0, 4);
	}

	public void init() {
		if (!loadShaders()) {
			Log.v(TAG, "loadShaders fail");
                        return;
                }		
	}
	
	private void prepareRender(Rect area, float[] vertices, float[] color) {
		float[] projection = new float[16];
		float[] modelView = new float[16];
                float[] mvp = new float[16];
                
                // 这里指定屏幕上绘图区域的四个定点坐标
                float width = area.width();
                float height = area.height();

		glViewport(area.left, area.top, (int)width, (int)height);
		Matrix.orthoM(projection, 0, 0, width, 0, height, -1.0f, 1.0f);

		Matrix.setIdentityM(modelView, 0);

                //mtxMultiply(mvp, _projection, modelView);
		Matrix.multiplyMM(mvp, 0, projection, 0, modelView, 0);

                // Use the program that we previously created
                glUseProgram(_program);
                
                // Set the modelview projection matrix that we calculated above
                // in our vertex shader
                //glUniformMatrix4fv(_uniform[UNIFORM_IDX_MODELVIEW_PROJECTION_MATRIX], 1, GL_FALSE, mvp);
		glUniformMatrix4fv(_uniform[UNIFORM_IDX_MODELVIEW_PROJECTION_MATRIX], 1, false, mvp, 0);
		//glUniformMatrix4fv(_uniform[UNIFORM_IDX_MODELVIEW_PROJECTION_MATRIX], 1, false, _projection, 0);
		glUniform4fv(_uniform[UNIFORM_IDX_COLOR], 1, color, 0);
                
		glEnableVertexAttribArray(_attrib[ATTRIB_IDX_POSITION]);
		glVertexAttribPointer(_attrib[ATTRIB_IDX_POSITION], 2, GL_FLOAT, false, 0, fBuffer(vertices));		
	}

	private boolean loadShaders() {
		if (!buildProgram(commonVSH, commonFSH)) {
			return false;
		}
                
                // NOTE: this step is very important
                glUseProgram(_program);
                
                // get uniform locations
                _uniform[UNIFORM_IDX_MODELVIEW_PROJECTION_MATRIX] = glGetUniformLocation(_program, "modelViewProjectionMatrix");
                _uniform[UNIFORM_IDX_COLOR] = glGetUniformLocation(_program, "color");

		// get attrib locations
		_attrib[ATTRIB_IDX_POSITION] = glGetAttribLocation(_program, "inPosition");
                
                return true;
        }

	private static final int ATTRIB_IDX_POSITION = 0;
	private static final int ATTRIB_NUM = 1;
	private int[] _attrib = new int[ATTRIB_NUM];

	private static final int UNIFORM_IDX_MODELVIEW_PROJECTION_MATRIX = 0;
	private static final int UNIFORM_IDX_COLOR = 1;
	private static final int UNIFORM_NUM = 2;
	private int[] _uniform = new int[UNIFORM_NUM];

	private static final String commonVSH =
		"#ifdef GL_ES\n" +
		"precision highp float;\n" +
		"#endif\n" +
		"uniform mat4 modelViewProjectionMatrix;\n" +
		"#if __VERSION__ >= 140\n" +
		"in vec4 inPosition;\n" +
		"#else\n" +
		"attribute vec4 inPosition;\n" +
		"#endif\n" +
		"void main(void) {\n" +
		"    gl_Position = modelViewProjectionMatrix * inPosition;\n" +
		"}\n";
	
	private static final String commonFSH =
		"#ifdef GL_ES\n" +
		"precision highp float;\n" +
		"#endif\n" +
		"uniform vec4 color;\n" +
		"void main(void) {\n" +
		"#if __VERSION__ >= 140\n" +
		"    fragColor = color;\n" +
		"#else\n" +
		"    gl_FragColor = color;\n" +
		"#endif\n" +
		"}\n";

	private static final String TAG = "CommonRendererGLES20";
}