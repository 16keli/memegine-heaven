package engine.physics;

import engine.geom2d.Point2;
import engine.geom2d.Vector2;
import engine.physics.entity.EntityPhysics;

/**
 * A collision between two {@code Entity}s because Physics is hard
 * <p>
 * The {@code Entity}s are treated as 4 points at the edge of their hitboxes because physics is hard
 * 
 * @author Kevin
 */
public class Collision {
	
	/**
	 * The X and Y positions of the collision location.
	 */
	Point2 pos;
	
	/**
	 * The fractions of a tick after this tick happens where the collision will take place
	 */
	double tickFractions;
	
	/**
	 * The {@code EntityPhysics}s involved
	 */
	EntityPhysics ent1, ent2;
	
	CollisionType type;
	
	public static final int STEPS = 1;
	
	public Collision(EntityPhysics ent1, EntityPhysics ent2) {
		this(CollisionType.ELASTIC, ent1, ent2);
	}
	
	public Collision(CollisionType type, EntityPhysics ent1, EntityPhysics ent2) {
		System.out.println("Suspected Collision Between " + ent1.getClass().getName() + " and "
				+ ent2.getClass().getName());
		this.ent1 = ent1;
		this.ent2 = ent2;
		this.type = type;
		for (double i = 0; i < STEPS; i++) {
			ent1.tick1(i / STEPS);
			ent2.tick1(i / STEPS);
			if (Physics.suspectedCollision(ent1, ent2)) {
				this.tickFractions = i;
				this.pos = Point2.of((ent1.newp.getX() + ent2.newp.getX()) / 2, (ent1.newp.getY() + ent2.newp.getY()) / 2);
//				System.out.println(this.pos);
				break;
			}
			ent1.tick2();
			ent2.tick2();
		}
		this.calculate(ent1, ent2);
		for (double i = this.tickFractions; i < STEPS; i++) {
			ent1.tick1(i / STEPS);
			ent2.tick1(i / STEPS);
			ent1.tick2();
			ent2.tick2();
		}
	}
	
	public void calculate(EntityPhysics ent1, EntityPhysics ent2) {
		boolean exit = false;
		if (!ent1.collisionMoveable()) {
			ent2.vel = ent2.vel.scaleVector(-1);
			exit = true;
		}
		if (!ent2.collisionMoveable()) {
			ent1.vel = ent1.vel.scaleVector(-1);
			exit = true;
		}
		if (exit) {
			return;
		}
		double sumPX = ent1.getMomentum().getX() + ent2.getMomentum().getX();
		double sumPY = ent1.getMomentum().getY() + ent2.getMomentum().getY();
		switch (this.type) {
			case PERFECTLYINELASTIC:
				double m = ent1.mass + ent2.mass;
				ent1.vel = Vector2.of(sumPX / m, sumPY / m);
				ent2.vel = Vector2.of(sumPX / m, sumPY / m);
				break;
			case INELASTIC:
				// Physics is hard
				break;
			case ELASTIC:
				
				break;
			default:
				return;
		}
	}
	
	public enum CollisionType {
		PERFECTLYINELASTIC, INELASTIC, ELASTIC;
	}
}
