package ch.epfl.cs107.play.game.actor;

import ch.epfl.cs107.play.math.Entity;
import ch.epfl.cs107.play.math.EntityBuilder;
import ch.epfl.cs107.play.math.PartBuilder;
import ch.epfl.cs107.play.math.Vector;
import ch.epfl.cs107.play.math.World;

import org.jbox2d.dynamics.contacts.Velocity;

import ch.epfl.cs107.play.game.actor.bike.Bike;
import ch.epfl.cs107.play.game.actor.general.Wheel;
import ch.epfl.cs107.play.math.BasicContactListener;
import ch.epfl.cs107.play.math.Circle;
import ch.epfl.cs107.play.math.Contact;
import ch.epfl.cs107.play.math.ContactListener;
import ch.epfl.cs107.play.math.Polygon;
import ch.epfl.cs107.play.math.Shape;


public abstract class GameEntity {

	private ActorGame game;
	private Entity body;
	private Contact contact;
	private ContactListener contactListener;

	// protected allow a better protection and prevent the modification outside of the package
	// protected still allows whole access in the same package, just like "public" would do normally and this is not always wanted



	public GameEntity(ActorGame game, boolean fixed , Vector position) {
		this.game = game;

		EntityBuilder entityBuilder = game.CreateEntityBuilder();
		entityBuilder.setFixed(fixed);
		entityBuilder.setPosition(position);
		body = entityBuilder.build();

	}


	public GameEntity(ActorGame game, boolean fixed) {
		this.game = game;

		EntityBuilder entityBuilder = game.CreateEntityBuilder();
		entityBuilder.setFixed(fixed);
		body = entityBuilder.build();	
	}


	public void destroy() {
		body.destroy();
	}

	protected Entity getEntity() {
		return body;
	}


	public float getWheelSpeed(Wheel wheel) {
		return wheel.getEntity().getAngularPosition();
	}


	public boolean contactBikeWithPolyline(Contact contact ,  Wheel leftWheel , Wheel rightWheel) {
		return (contact.getOther().getEntity() == leftWheel.getEntity() || contact.getOther().getEntity() == rightWheel.getEntity());



	}

	public void addContactListener(ContactListener listener) {
		body.addContactListener(listener);
		contactListener = listener;

	}








	//protected ou public ?
	public Vector getVelocity() {
		Vector velocity = body.getVelocity();
		return velocity;

	}

	protected ActorGame getOwner() {
		return game;
	}
}