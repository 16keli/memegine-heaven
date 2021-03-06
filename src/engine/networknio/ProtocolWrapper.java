package engine.networknio;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import engine.networknio.packet.PacketNIO;

/**
 * A wrapper around a given communication protocol Channel and Buffers, be it {@code SocketChannel} or
 * {@code DatagramChannel}, that simplifies sending/receiving data
 * 
 * @author Kevin
 */
public abstract class ProtocolWrapper {
	
	
	/**
	 * The {@code ByteBuffer} that data will be read into
	 */
	protected ByteBuffer inputBuffer;
	
	/**
	 * The {@code ByteBuffer} that data will be sent from
	 */
	protected ByteBuffer outputBuffer;
	
	protected ConnectionNIO connect;
	
	/**
	 * Creates a new {@code ProtocolWrapper} with the given Input and Output {@code ByteBuffer}s
	 * 
	 * @param input
	 * @param output
	 */
	public ProtocolWrapper(ByteBuffer input, ByteBuffer output, ConnectionNIO connect) {
		this.inputBuffer = input;
		this.outputBuffer = output;
		this.connect = connect;
	}
	
	/**
	 * Sends the data written in the output buffer to the given remote address
	 * 
	 * @param remote
	 *            The destination
	 * @throws IOException
	 */
	public abstract void sendData(SocketAddress remote) throws IOException;
	
	/**
	 * Attempts to read data from the appropriate Channel into the input buffer
	 * 
	 * @throws IOException
	 * @return Whether the read was successful
	 */
	public abstract boolean readData() throws IOException;
	
	/**
	 * Writes a {@code PacketNIO} to the output buffer
	 * 
	 * @param p
	 * @throws IOException
	 */
	public void writePacket(PacketNIO p) throws IOException {
		this.outputBuffer.putInt(p.getID());
//		System.out.println("Writing packet with id " + p.getID());
		p.writePacketData(this.outputBuffer);
		
//		System.out.println("Write packet " + p.getClass().getSimpleName());
//		PacketNIO.getPacketDataToLimit(buffer);
	}
	
	/**
	 * Reads a {@code PacketNIO} from the input buffer
	 * 
	 * @throws IOException
	 * @return A read PacketNIO
	 */
	public PacketNIO readPacket() throws IOException {
		int id = this.inputBuffer.getInt();
//		System.out.println("Reading packet with id " + id);
		if (id == Integer.MIN_VALUE) {// This signifies the end of the stream
			return null;
		}
		PacketNIO p = PacketNIO.getNewPacket(id);
		p.readPacketData(this.inputBuffer);
		
//		System.out.println("Read packet " + p.getClass().getSimpleName());
//		getPacketDataFromBuffer(idAndSize, data);
		return p;
	}
	
	/**
	 * Creates a List of {@code PacketNIO}s from the data in the input buffer
	 * 
	 * @throws IOException
	 * @return
	 */
	public List<PacketNIO> readFully() throws IOException {
		List<PacketNIO> packs = new LinkedList<PacketNIO>();
		PacketNIO toAdd = null;
		while ((toAdd = this.readPacket()) != null) {
			packs.add(toAdd);
		}
		return packs;
	}
	
	/**
	 * The TCP Channel Wrapper
	 * 
	 * @author Kevin
	 */
	public static class TCPChannelWrapper extends ProtocolWrapper {
		
		
		/**
		 * The {@code SocketChannel}
		 */
		private SocketChannel tcp;
		
		public TCPChannelWrapper(SocketChannel channel, ByteBuffer in, ByteBuffer out, ConnectionNIO c) {
			super(in, out, c);
			this.tcp = channel;
		}
		
		@Override
		public void sendData(SocketAddress remote) throws IOException {
			this.outputBuffer.flip();
			this.tcp.write(this.outputBuffer);
			this.outputBuffer.clear();
		}
		
		@Override
		public boolean readData() throws IOException {
			this.inputBuffer.clear();
			int tcpCount = this.tcp.read(this.inputBuffer);
			boolean flag = tcpCount > 0;
			this.inputBuffer.flip();
			return flag;
		}
	}
	
	/**
	 * The UDP Channel Wrapper
	 * 
	 * @author Kevin
	 */
	public static class UDPChannelWrapper extends ProtocolWrapper {
		
		
		/**
		 * The {@code SocketChannel}
		 */
		private DatagramChannel udp;
		
		public UDPChannelWrapper(DatagramChannel channel, ByteBuffer in, ByteBuffer out, ConnectionNIO c) {
			super(in, out, c);
			this.udp = channel;
		}
		
		@Override
		public void sendData(SocketAddress remote) throws IOException {
			this.outputBuffer.flip();
			this.udp.send(this.outputBuffer, remote);
			this.outputBuffer.clear();
		}
		
		@Override
		public boolean readData() throws IOException {
			this.inputBuffer.clear();
			boolean flag = this.udp.receive(this.inputBuffer) != null;
			this.inputBuffer.flip();
			return flag;
		}
		
	}
	
}
