package engine.example;

import java.awt.Color;
import java.net.InetAddress;

import javax.swing.JOptionPane;

import engine.client.graphics.FontWrapper;
import engine.client.graphics.Screen;
import engine.client.menu.Menu;
import engine.client.menu.MenuComponent;
import engine.input.ActionMenuInput;

public class PongMenu extends Menu {
	
	static MenuComponent solo = new MenuComponent("Play Against the AI", 32);
	
	static MenuComponent local = new MenuComponent("Play Local Multiplayer", 48);
	
	static MenuComponent host = new MenuComponent("Host Online Multiplayer", 64);
	
	static MenuComponent join = new MenuComponent("Join Online Multiplayer", 80);
	
	static MenuComponent[][] list = { { solo, local, host, join } };
	
	public PongMenu() {
		super(list);
	}
	
	@Override
	public void tickMenu() {
		if (this.menuInput.getInputFromAction(ActionMenuInput.SELECT).isClicked()) {
			InetAddress inet = InetAddress.getLoopbackAddress(); // Default
			int port = 25565;// Default
			if (this.selected == solo) {
				Pong.prepareServer(port, 1);
			} else if (this.selected == local) {
				Pong.prepareServer(port, 1);
				((Pong) this.client.game).p2exists = true;
			} else if (this.selected == host) {
				Pong.prepareServer(port, 2);
			} else if (this.selected == join) {
				try {
					inet = InetAddress.getByName(JOptionPane.showInputDialog(this.client, "Connect"));
					port = Integer.parseInt(JOptionPane.showInputDialog(this.client, "Port"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			this.client.connect(inet, port);
			
			this.client.setMenu(null);
		}
	}
	
	@Override
	public void renderMenu(Screen screen) {
		FontWrapper.draw("It's Pong you Idiot!", screen,
				FontWrapper.getXCoord(screen, "It's Pong you Idiot!"), 16, Color.WHITE.getRGB());
	}
	
}
