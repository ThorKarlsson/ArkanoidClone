package com.Arkanoid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.BufferUtils;
import java.nio.FloatBuffer;
import java.util.Vector;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.Arkanoid.ObjectReference;
import com.Arkanoid.Point2D;
import com.Arkanoid.block;

public class Arkanoid_Start implements ApplicationListener{
	public ObjectReference paddle;
	public ObjectReference ball;
	public ObjectReference block;
	public ObjectReference life;
	block [] board = new block[78];
	
	float ballPosX = 355; //initial values = starting position of items
	float ballPosY = 200;
	float ballSpeed = 200;
	float slope = 1;
	float rateOfChange = 1;
	float paddlePosX = 310; //only x value of paddle pos will change, y will be constant
	float paddlePosY = 50;
	float paddleSpeed = 250;
	
	int lives = 3;
	int level = 1;
	boolean start = true;
	
	public enum XDirection{Left, Right};
	public enum YDirection{Up, Down};
	XDirection xdir = XDirection.Right;
	YDirection ydir = YDirection.Down;
	
	boolean first = true; //the first time draw is called the board array's x and y values should be initialized
	public static void main(String args[])
	{
		new LwjglApplication(new Arkanoid_Start(), "Arkanoid", 640, 480, false);
	}
	
	@Override
	public void create() {
		Gdx.gl11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		Vector<Point2D> vertexList = new Vector<Point2D>();
		
		paddle = createBox(90, 15, vertexList);
		ball = createBall(7, 30, vertexList);
		block = createBox(30, 15, vertexList);
		life = createBox(40, 10, vertexList); // markers at the bottom of the screen tell you how many lives you have left
		for(int i = 0; i < board.length; i++)//initialize board
		{
			board[i] = new block();
		}
		int floatBufferSize = vertexList.size() * 2;
		FloatBuffer vertexBuffer = BufferUtils.newFloatBuffer(floatBufferSize);
		float [] array = new float[floatBufferSize];
		for(int i = 0; i < vertexList.size(); i++)
		{
			array[i*2] = vertexList.get(i).x;
			array[i*2+1] = vertexList.get(i).y;
		}		
		vertexBuffer.put(array);
		vertexBuffer.rewind();
	
		Gdx.gl11.glVertexPointer(2, GL11.GL_FLOAT, 0, vertexBuffer);
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render() {
		draw();
		update();
	}
	
	public void draw()
	{
		Gdx.gl11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); //sets backdrop color
		Gdx.gl11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		
		Gdx.gl11.glMatrixMode(GL11.GL_MODELVIEW);
		Gdx.gl11.glLoadIdentity();
		Gdx.glu.gluOrtho2D(Gdx.gl10, 0, 640, 0, 480);
		Gdx.gl11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f); //sets drawing color
		
		//draw paddle
		Gdx.gl11.glPushMatrix();
		Gdx.gl11.glTranslatef(paddlePosX, paddlePosY, 0);
		drawObject(paddle);
		Gdx.gl11.glPopMatrix();
		
		//draw lives
		for(int i = lives, x = 490; i > 0; i--, x+=60)
		{
			Gdx.gl11.glPushMatrix();
			Gdx.gl11.glTranslatef(x, 10, 0);
			drawObject(life);
			Gdx.gl11.glPopMatrix();
		}
		//draw ball
		Gdx.gl11.glPushMatrix();
		Gdx.gl11.glTranslatef(ballPosX, ballPosY, 0);
		drawObject(ball);
		Gdx.gl11.glPopMatrix();
		
		//draw blocks
		int tempx = 40, tempy = 450;
		setBrickColor();
		//Gdx.gl11.glColor4f(0.0f, 0.0f, 1.5f, 1.0f); //for fun the color of the bricks is a function of level
		
		for(int i = 0; i < 78; i++)
		{
			if(board[i].hit == false)//checks if specific block has been hit
			{
				Gdx.gl11.glPushMatrix();
				Gdx.gl11.glTranslatef(tempx, tempy, 0);
				drawObject(block);
				Gdx.gl11.glPopMatrix();
			}
			if(first)//if this is the first time draw is called
			{
				board[i].minX = tempx - 15;
				board[i].maxX = tempx + 15;
				board[i].Y = tempy;
			}
			tempx += 45;
			if((i+1) % 13 == 0)
			{
				tempy -= 25;
				tempx = 40;
			}
		}
		first = false;
		
	}
	
	public void update()
	{
		float deltaTime = Gdx.graphics.getDeltaTime();
		
		if(start)
		{
			start();
			if(Gdx.input.isKeyPressed(Input.Keys.SPACE))
			{
				start = false;
				ballSpeed = 200 + (level*10); //with each level the ball speed increases by 10
				ydir = YDirection.Up;
			}
		}
		//Paddle movement
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT) && paddlePosX >= 45)
		{
			paddlePosX -= paddleSpeed * deltaTime;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT) && paddlePosX <= (Gdx.graphics.getWidth() - 45))
		{
			paddlePosX += paddleSpeed * deltaTime;
		}
		
		//ball direction
		if(ballPosX <= 7)
		{
			xdir = XDirection.Right;
		}
		if(ballPosX >= (Gdx.graphics.getWidth() - 7))
		{
			xdir = XDirection.Left;
		}
		if(ballPosY <= 7) //if ball goes off screen lose a life
		{
			lives--;
			if(lives < 0)
			{
				gameOver();
			}
			else
			{
				start();
			}
		}
		if(ballPosY >= (Gdx.graphics.getHeight() - 7))
		{
			ydir = YDirection.Down;
		}
		
		//if ball hits paddle
		if((ballPosY < 64 && ballPosY > 51) && (ballPosX > (paddlePosX-45)&& ballPosX < (paddlePosX+45))) 
		{
			ydir = YDirection.Up;
			paddleHit(ballPosX - paddlePosX); //the number being sent in will describe where exactly the ball hit on the paddle
		}
		
		//checks if ball hits a block
		for(int i = 0; i < 78; i++)
		{
			if(!board[i].hit)
			{
				//if ball is on same x values as block, check for top/bottom collision
				if(ballPosX-7 < board[i].maxX && ballPosX+8 > board[i].minX)
				{
					 //if the bottom of the block is struck
					if(ballPosY > board[i].Y - 18 && ballPosY < board[i].Y - 10)
					{
						board[i].hit = true;
						ydir = YDirection.Down;
					}
					//if top of block is hit
					else if(ballPosY > board[i].Y + 10 && ballPosY < board[i].Y +18)
					{
						board[i].hit = true;
						ydir = YDirection.Up;
					}
				}
				//if right of block is hit
				if(ballPosX > board[i].maxX && ballPosX < board[i].maxX + 8) 
				{
					if(ballPosY > board[i].Y - 14 && ballPosY < board[i].Y + 14)
					{
						board[i].hit = true;
						xdir = XDirection.Right;
					}
				}
				//if left of block is hit
				if(ballPosX < board[i].minX && ballPosX > board[i].minX - 8)
				{
					if(ballPosY > board[i].Y - 14 && ballPosY < board[i].Y + 14)
					{
						board[i].hit = true;
						xdir = XDirection.Left;
					}
				}
			}
		}
		
		for(int i = 0; i < board.length; i++) // checks if the board has been cleared
		{
			if(!board[i].hit)
			{
				break;
			}
			if(i == 77)
			{
				levelCleared();
			}
		}
		
		//ball movement
		if(xdir == XDirection.Left)
		{
			ballPosX -= (ballSpeed * slope) * deltaTime;
		}
		else if(xdir == XDirection.Right)
		{
			ballPosX += (ballSpeed * slope) * deltaTime;
		}
		rateOfChange = (ballSpeed * deltaTime) - (ballSpeed*slope*deltaTime);
		if(ydir == YDirection.Down)
		{ //rateOfChange is used to keep the ball at a constant speed, changing the rate of change in y based on how x is changing
			ballPosY -= (ballSpeed * deltaTime) + rateOfChange/2;
		}
		if(ydir == YDirection.Up)
		{
			ballPosY += (ballSpeed * deltaTime) + rateOfChange/2;
		}
		
		ballSpeed += .001; //ball speed gets faster over time
	}

	@Override
	public void resize(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}
	
	public void paddleHit(float whereHit)
	{
		/*Slope dictates the rate at which x changes. When the ball hits left of the paddle the ball is supposed to bear left
		 * and then slope is calculated to change the angle of the ball. The paddle is 90 pixels long and 22.5 is 45/2
		 * so when the ball hits at 22.5 the slope is one and the x and y values will change at the same rate as in y = x
		 * below 22.5 and the x will change faster and above 22.5 and the x will change slower
		 * */
		if( whereHit < 0) //if ball hits left side of paddle 
		{
			slope = whereHit / -22.5f;
			xdir = XDirection.Left;
		}
		else if(whereHit > 0) //if ball hits right of paddle
		{
			slope = (whereHit) / 22.5f;
			xdir = XDirection.Right;
		}
		else if(whereHit == 0)
		{//when slope = 0 the ball goes straight up
			slope = 0;
		}
	}
	
	public void gameOver()
	{
		paddleSpeed = 0;
		ballSpeed = 0;
		start = true;
	}
	
	public void levelCleared()
	{
		for(int i = 0; i < board.length; i++)
		{
			board[i].hit = false;
		}
		level++;
		start();
		
	}
	
	public void start()
	{
		ballPosX = paddlePosX;
		ballPosY = paddlePosY+15;
		ballSpeed = 0;
		slope = 0.5f;
		start = true;
	}
	
	public void setBrickColor() 
	{//for fun the brick color changes color for each level
		switch((level) % 10)
		{
		case 1:
			Gdx.gl11.glColor4f(1.0f, 0.5f, 0.5f, 1.0f);
			break;
		case 2:
			Gdx.gl11.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
			break;
		case 3:
			Gdx.gl11.glColor4f(0.5f, 1.0f, 0.5f, 1.0f);
			break;
		case 4:
			Gdx.gl11.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
			break;
		case 5:
			Gdx.gl11.glColor4f(0.5f, 0.5f, 1.0f, 1.0f);
			break;
		case 6:
			Gdx.gl11.glColor4f(0.0f, 0.0f, 1.0f, 1.0f);
			break;
		case 7:
			Gdx.gl11.glColor4f(0.2f, 0.2f, 0.1f, 1.0f);
			break;
		case 8:
			Gdx.gl11.glColor4f(0.2f, 0.4f, 0.4f, 1.0f);
			break;
		case 9:
			Gdx.gl11.glColor4f(1.0f, 0.0f, 1.0f, 1.0f);
			break;
		case 0:
			Gdx.gl11.glColor4f(0.0f, 1.0f, 1.0f, 1.0f);
			break;
		}
	}
	
	//object creators
	public ObjectReference createBox(float width, float height, Vector<Point2D> vertexList)
	{
		ObjectReference or = new ObjectReference(vertexList.size(), 4, GL11.GL_TRIANGLE_STRIP);
		
		vertexList.add(new Point2D(-width/2.0f, -height/2.0f));
		vertexList.add(new Point2D(-width/2.0f, height/2.0f));
		vertexList.add(new Point2D(width/2.0f, -height/2.0f));
		vertexList.add(new Point2D(width/2.0f, height/2.0f));
		
		return or;
	}
	public ObjectReference createBall(float radius, int slices, Vector<Point2D> vertexList)
	{
		ObjectReference or = new ObjectReference(vertexList.size(), slices, GL11.GL_TRIANGLE_FAN);
		
		for(float f = 0; f < 2*Math.PI; f += (float)2*Math.PI / (float)slices)
		{
			vertexList.add(new Point2D((float)Math.cos(f) * radius, (float)Math.sin(f) * radius));
		}
		
		return or;
	}
	
	private void drawObject(ObjectReference or)
	{
		Gdx.gl11.glDrawArrays(or.openGLPrimitiveType, or.firstIndex, or.vertexCount);
	}

}
