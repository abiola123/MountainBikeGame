package ch.epfl.cs107.play.game.actor.bike;


import java.awt.Color;
import java.util.Date;

import com.sun.glass.events.KeyEvent;

import ch.epfl.cs107.play.game.actor.Actor;
import ch.epfl.cs107.play.game.actor.ActorGame;
import ch.epfl.cs107.play.game.actor.BikeGame;
import ch.epfl.cs107.play.game.actor.GameEntity;
import ch.epfl.cs107.play.game.actor.ImageGraphics;
import ch.epfl.cs107.play.game.actor.ShapeGraphics;
import ch.epfl.cs107.play.game.actor.TextGraphics;
import ch.epfl.cs107.play.game.actor.general.Wheel;
import ch.epfl.cs107.play.math.BasicContactListener;
import ch.epfl.cs107.play.math.Circle;
import ch.epfl.cs107.play.math.Contact;
import ch.epfl.cs107.play.math.ContactListener;
import ch.epfl.cs107.play.math.Entity;
import ch.epfl.cs107.play.math.PartBuilder;
import ch.epfl.cs107.play.math.Polygon;
import ch.epfl.cs107.play.math.Polyline;
import ch.epfl.cs107.play.math.Shape;
import ch.epfl.cs107.play.math.Transform;
import ch.epfl.cs107.play.math.Vector;
import ch.epfl.cs107.play.window.Canvas;
//creates a new bike for the game
public class Bike extends GameEntity implements Actor {

	private Entity body = getEntity();
	private Polyline arm, back, rightTLeg, leftTLeg, leftBLeg, rightBLeg;
	private ShapeGraphics headGraphics;
	private ShapeGraphics shapeGraphics;
	private Wheel leftWheel;
	private Wheel rightWheel;
	private Boolean directionR = true;
	//sets the maximum wheel speed
	private Float MAX_WHEEL_SPEED = 15.0f;
	private Vector position;
	private Vector rightFootLocation;
	private Vector leftFootLocation;
	private Vector rightKneeLocation;
	private Vector leftKneeLocation;
	private Vector handLocation = new Vector(0.5f,1.0f);
	private boolean gotHit;
	private boolean wasHit;
	float timeSinceCollision = 0;

	//creates a contact listener for the bike, the main body frame doesn't react to ghost elements, or the wheels
	ContactListener listener = new ContactListener () {
		@Override
		public void beginContact(Contact contact) {
			if (contact.getOther().isGhost ()) {
				return;
			} else if (contactBikeWithPolyline(contact,leftWheel,rightWheel)) {
				gotHit = false;
				return;
			} else {
				// if contact with anything but the wheels or a ghost element 
				gotHit = true ;
				return;
			}
		}
		@Override
		public void endContact(Contact contact) {}

	};
	

	public Bike(ActorGame game, boolean fixed , Vector position, Float radius) {
		
		super(game, fixed,position);
		
		if(radius <= 0) {
			throw new IllegalArgumentException("Parametre invalide");
		}
		
		//creates the "invisible" body
		this.position = position;
		PartBuilder partBuilder = body.createPartBuilder();
		Polygon polygon = new Polygon (
				new Vector	(0.0f, 0.5f),
				new Vector 	(0.5f, 1.0f),
				new Vector	(0.0f, 2.0f),
				new Vector	(-0.5f, 1.0f)
				);

		body.addContactListener(listener);

		partBuilder.setShape(polygon);
		partBuilder.build();
		partBuilder.setGhost(true);

		Vector leftWheelPos = position.add(-1.0f, 0.f);
		Vector rightWheelPos = position.add(1.0f,0.f);

		// Building the wheels and attaching them to the bike
		leftWheel = new Wheel(this.getOwner(), false,leftWheelPos, radius);
		rightWheel = new Wheel(this.getOwner(), false,rightWheelPos, radius);
		//added boolean for internal collision between parts that have been linked by constraint
		//																		\./
		leftWheel.attach(body, new Vector (-1.0f, 0.0f), new Vector(-0.5f,-1.0f), false);
		rightWheel.attach(body, new Vector (1.0f, 0.0f), new Vector(0.5f,-1.0f) , false);

		//starting to create shapes to draw our character
		Circle head = new Circle(0.2f, getHeadLocation());
		headGraphics = new ShapeGraphics(head,Color.WHITE,Color.WHITE,.1f,1.f,0);
		headGraphics.setParent(body);
	}


	// All main body parts locations, in local coordinates

	// Body center location, in local coordinates
	private Vector getBodyCentreLocation() {
		return new Vector(-0.45f,0.9f);
	}

	// Shoulder location, in local coordinates
	private Vector getShoulderLocation() {
		return new Vector(-0.1f, 1.45f);
	}

	// Hand location, in local coordinates
	public void setHandLocation() {
		handLocation = new Vector(0.7f,1.7f);
	}

	// Head location, in local coordinates
	private Vector getHeadLocation() {
		return new Vector(0.0f, 1.75f);
	}

	//Draws the full bike (character and wheels)
	@Override
	public void draw(Canvas canvas) {
		headGraphics.draw(canvas);
		rightWheel.draw(canvas);
		leftWheel.draw(canvas);
		
		//animates the legs
		animate();
		
		//if the character is going right
		if(directionR) {
			drawChar(arm, getShoulderLocation(), handLocation, canvas);
			drawChar(back, getShoulderLocation(), getBodyCentreLocation(), canvas);
			drawChar(leftTLeg, getBodyCentreLocation(), leftKneeLocation,canvas);
			drawChar(rightTLeg, getBodyCentreLocation(), rightKneeLocation, canvas);
			drawChar(leftBLeg, leftKneeLocation, leftFootLocation, canvas);
			drawChar(rightBLeg, rightKneeLocation, rightFootLocation, canvas);
		
		//if the character is going left, draws him inverted
		} else if (!directionR) {
			drawChar(arm, getShoulderLocation().mul(-1, 1), handLocation.mul(-1,1), canvas);
			drawChar(back, getShoulderLocation().mul(-1, 1), getBodyCentreLocation().mul(-1, 1), canvas);
			drawChar(leftTLeg, getBodyCentreLocation().mul(-1, 1), leftKneeLocation.mul(-1, 1),canvas);
			drawChar(rightTLeg, getBodyCentreLocation().mul(-1, 1), rightKneeLocation.mul(-1, 1), canvas);
			drawChar(leftBLeg, leftKneeLocation.mul(-1, 1), leftFootLocation.mul(-1, 1), canvas);
			drawChar(rightBLeg, rightKneeLocation.mul(-1, 1), rightFootLocation.mul(-1, 1), canvas);
		}
	}
	
	//allows us to draw our character's body parts quicker
	void drawChar(Shape shape, Vector location1, Vector location2, Canvas canvas) {
		shape = new Polyline(location1, location2);
		shapeGraphics = new ShapeGraphics(shape, null, Color.WHITE,.1f,1.f,0);
		shapeGraphics.setParent(body);
		shapeGraphics.draw(canvas);
	}

	//animates our character's legs
	public void animate() {

		float wheelSpeed = 0;
		
		//the legs will move according to the motor wheel
		if(directionR) {
			wheelSpeed = getWheelAngle(leftWheel);
		}

		if(!directionR) {
			wheelSpeed = -getWheelAngle(rightWheel);
		}
		
		//maths
		rightFootLocation = new Vector(0f,0.1f).add((float)(0.2*Math.cos(wheelSpeed)),(float)(0.2*Math.sin(wheelSpeed)));
		leftFootLocation = new Vector(0f, 0.1f).add(-(float)(0.2*Math.cos(wheelSpeed)),-(float)(0.2*Math.sin(wheelSpeed)));

		rightKneeLocation = new Vector(0.1f, 0.6f).add((float)(0.05*Math.cos(wheelSpeed)),(float)(0.05*Math.sin(wheelSpeed)));
		leftKneeLocation = new Vector(0.1f, 0.6f).add(-(float)(0.05*Math.cos(wheelSpeed)),-(float)(0.05*Math.sin(wheelSpeed)));
	}


	@Override
	public Vector getVelocity() {
		// TODO Auto-generated method stub
		return body.getVelocity();

	}

	@Override
	public Transform getTransform() {
		return body.getTransform();
	}


	//destroys the entire bike
	public void destroy() {
		body.destroy();
		leftWheel.destroy();
		rightWheel.destroy();
	}


	//updates the contact listener and keyboard events, allowing us to control our bike
	public void update(float deltaTime) {
		
		//ContactListener
		
		//if the character was hit for a second, makes it so he is always hit
		if(gotHit) {
			setWasHit(true);
		}
		
		//Death animation, the delayer lets it happen before destroying the bike after collision
		if(wasHit) {
			rightWheel.detach();
			leftWheel.detach();

			// delayer
			timeSinceCollision += deltaTime;
			if(timeSinceCollision > 3.5f) {
				this.destroy();
			}
		}

		//keyboard, changes direction if spacebar is pressed
		if (getOwner().getKeyboard().get(KeyEvent.VK_SPACE).isPressed ()) {
			directionR = !directionR;
		}

		// 2. Deactivates by default the movement of the wheels
		leftWheel.relax();
		rightWheel.relax();


		// 3. Blocks the wheels if the downKey is pressed
		if (getOwner().getKeyboard ().get(KeyEvent.VK_DOWN).isDown ()) {
			leftWheel.power(0);
			rightWheel.power(0);
		}

		// 4. Makes the bike move forward if the upKey is pressed
		if (getOwner().getKeyboard ().get(KeyEvent.VK_UP).isDown ()) {
			
			// if the character is facing right, powers the left wheel
			if (directionR) {
				leftWheel.power(-MAX_WHEEL_SPEED);
			}
			
			// if the character is facing right, powers the right wheel
			if (!directionR) {
				rightWheel.power(MAX_WHEEL_SPEED);
			}

			//boost for bike when you press "B" key
			if (getOwner().getKeyboard().get(KeyEvent.VK_B).isDown()) {
				if (directionR) {
					leftWheel.power(-35f);
				}

				if (!directionR) {
					rightWheel.power(35f);
				}
			}

		}

		// 5. Apply angular forces with right and left Arrows
		if (getOwner().getKeyboard ().get(KeyEvent.VK_RIGHT).isDown ()) {
			body.applyAngularForce(-25.0f);
		}

		if (getOwner().getKeyboard ().get(KeyEvent.VK_LEFT).isDown ()) {
			body.applyAngularForce(25.0f);
		}

	}
	
	// if the bike is hit once, sets it as was hit, allows us for better death animations
	private void setWasHit(boolean gothitonce) {
		if(gothitonce) {
			wasHit = true;
		}
	}

	public boolean wasHit() {
		return wasHit;
	}


}
