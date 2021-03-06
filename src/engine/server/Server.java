package engine.server;

import java.io.IOException;
import java.util.logging.Logger;

import engine.Engine;
import engine.Game;
import engine.event.EventBus;
import engine.event.SubscribeEvent;
import engine.event.game.ConnectionEstablishedEvent;
import engine.event.game.TickEvent;
import engine.input.Action;
import engine.networknio.ConnectionList;
import engine.networknio.ConnectionNIO;
import engine.networknio.packet.PacketNIO;
import engine.networknio.packet.PacketPlayer;

/**
 * Represents the Server, which manages {@code Client} connections and gives them something to do
 * <p>
 * All {@code Game}s are based on the {@code Client} and {@code Server} communication implementation. We
 * learned from Notch's mistakes.
 * 
 * @author Kevin
 */
public abstract class Server {
	
	
	/**
	 * The {@code Server} instance of {@code Logger}
	 */
	public static final Logger logger = Logger.getLogger("engine.server");
	
	/**
	 * The {@code EventBus} used by the {@code Server} to process {@code GameEvent}s
	 */
	public static EventBus SERVER_BUS = new EventBus("Server Bus");
	
	/**
	 * The connection listener thread
	 */
	private ServerNIOListenThread listener;
	
	/**
	 * The Game instance
	 */
	public Game game;
	
	/**
	 * The {@code ConnectionList}
	 */
	public ConnectionList connections;
	
	/**
	 * The minimum number of connections before the game starts. Set to -1 to not require any.
	 */
	public int minConnects;
	
	/**
	 * Creates a new Server that starts automatically
	 * 
	 * @param g
	 *            A {@code Game} instance
	 * @param port
	 *            The port to start the server on
	 */
	public Server(Game g, int port) {
		this(g, port, -1);
	}
	
	/**
	 * Creates a new Server
	 * 
	 * @param g
	 *            A {@code Game} instance
	 * @param port
	 *            The port to start the server on
	 * @param minConnects
	 *            The minimum number of connections before the game starts
	 */
	public Server(Game g, int port, int minConnects) {
		this.game = g;
		this.minConnects = minConnects;
		this.connections = new ConnectionList();
		Server.SERVER_BUS.register(this);
		this.startListenThread(port);
	}
	
	/**
	 * Starts the server's listener thread on the given port
	 * 
	 * @param port
	 *            The port
	 */
	public void startListenThread(int port) {
		try {
			this.listener = new ServerNIOListenThread(this, port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.listener.start();
	}
	
	/**
	 * A tick of game time on the server side.
	 * <p>
	 * Automatically calls {@code Game}'s tick method, so there is no need to call it again.
	 */
	public void tick() {
		this.game.gameTime = Engine.getGameTimeServer();
		this.game.temporaryEvents.post(new TickEvent(this.game.gameTime));
		this.game.tick(this);
		for (int i = 0; i < this.connections.getList().size(); i++) {
			PacketNIO p;
			while ((p = this.connections.getList().get(i).getReadPacket()) != null) {
				p.processServer(i, this);
//				System.out.println("Connection " + i + " sends packet " + PacketNIO.idtoclass.get(p.getID()).getName());
			}
			Action a;
			while ((a = this.game.players.get(i).actionQueue.getAction()) != null) {
				a.processActionOnServer(i, this);
			}
		}
		if (this.minConnects > this.connections.getList().size()) {
			// Not enough connections
		} else {
			this.game.start = true;
		}
		
		this.tickServer();
		
		this.connections.sendPackets();
	}
	
	/**
	 * Any Server-specific tasks that need to be done on a regular schedule
	 */
	protected abstract void tickServer();
	
	/**
	 * Shuts down the server
	 */
	public void shutdown() {
		for (ConnectionNIO conn : this.connections.getList()) {
			conn.networkShutdown();
		}
		this.listener.shutdown();
	}
	
	/**
	 * Disconnects the specified {@code Player}
	 * 
	 * @param player
	 *            The number to disconnect
	 */
	public void disconnect(int player) {
		ConnectionNIO conn = this.connections.getList().get(player);
		conn.networkShutdown();
		this.connections.getList().remove(player);
		this.game.players.remove(player);
	}
	
	/**
	 * Called when a new {@code Client} connects to this {@code Server}. This method should synchronize any
	 * game data necessary with the {@code Client} by sending the necessary {@code Packet} (s) through the
	 * {@code Player Connection}
	 * <p>
	 * It is possible to synchronize data through use of a single {@code PacketGame} of course, but all
	 * necessary classes that must be synchronized MUST implement {@code Serializable} or
	 * {@link engine.network.synchro.Rebuildable} or be synchronized through their own {@code Packet}s
	 * 
	 * @param c
	 *            The {@code Player} to send necessary game data to
	 */
	public abstract void synchronizeClientGameData(ConnectionNIO c);
	
	@SubscribeEvent
	public void onPlayerConnect(ConnectionEstablishedEvent e) {
		this.connections.sentTCPPacketAllExcept(new PacketPlayer(e.player), e.player.number);
	}
	
}
