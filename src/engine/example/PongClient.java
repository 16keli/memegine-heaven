package engine.example;

import java.awt.event.KeyEvent;
import java.io.File;

import engine.Game;
import engine.client.Client;
import engine.client.graphics.Screen;
import engine.client.graphics.sprite.SpriteSheet;

public class PongClient extends Client {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SpriteSheet cursorSheet;
	
	public PongClient(Game g, int w, int h, int s) {
		super(g, w, h, s);
	}
	
	public PongClient(Game g, int w, int h) {
		super(g, w, h);
	}
	
	@Override
	protected void tickClient() {
	
	}
	
	@Override
	public void renderGame(Screen screen) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void initClient() {
		this.cursorSheet = SpriteSheet
				.of(new File("C:/Users/Kevin/Desktop/Java/CATWGame/res/catw/cursor.png"), 32, 32);
//		cursor = cursorSheet.getSprite(0, 0);
		this.cursor = this.cursorSheet.getAnimated(5, 0, 0, 14, 1).scale(2);
//		new NewSound(new File("res/engine/sound/boop.wav")).play();
//		new SampledSound("res/engine/sound/mp3test.mp3").play();
	}
	
	@Override
	public void resetClient() {
		this.setMenu(new PongMenu());
	}
	
	@Override
	public boolean hasPlayerNumber() {
		return this.player.number != -1;
	}

	@Override
	public void registerDefaultKeyInputs() {
		this.keyInput.bindAction(KeyEvent.VK_W, ActionPongMove.UP);
		this.keyInput.bindAction(KeyEvent.VK_S, ActionPongMove.DOWN);
		this.keyInput.bindAction(KeyEvent.VK_I, ActionPongMove.UP2);
		this.keyInput.bindAction(KeyEvent.VK_J, ActionPongMove.DOWN2);
	}

	@Override
	public void registerDefaultMouseInputs() {
		// lol do you use mouse to play pong xd
	}
}
