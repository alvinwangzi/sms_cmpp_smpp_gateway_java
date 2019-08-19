package com.cl.inter.gateway;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 *
 * 长连接网关基类
 * @author zhu_tek
 *
 */
public class KeepConnectGateWay extends DefaultGateWay{
	
	//日志
	private final Logger logger = LogManager.getLogger(KeepConnectGateWay.class);

	//启动
	public Bootstrap bootstrap;

	//事件组
	private EventLoopGroup group;

	private ChannelFuture future;
	private Object notifyObj = new Object();
	private int connectTimes = 0;

	//是否关闭
	private boolean closed = true;

	/**
	 */
	public KeepConnectGateWay(){
		super();
	}

	@Override
    public void start() throws InterruptedException{
		connectTimes = 0;
		closed = false;
		setConnected(false);
		group = new NioEventLoopGroup();
		bootstrap = new Bootstrap();
		bootstrap.group(group).channel(NioSocketChannel.class);
		if( null!= getLocalAddr() && getLocalAddr().length() > 0 ){
			bootstrap.localAddress( getLocalAddr(), 0);
		}

		bootstrap.option(ChannelOption.TCP_NODELAY, true);
		bootstrap.option(ChannelOption.SO_KEEPALIVE,true);
		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

	}

	//连接到服务器端
	@Override
	public void doConnect(){
		setConnected(false);
		while(!isConnected() && !closed && connectTimes <= GateWay.MAX_CONNECT_TIMES){
			if(null != future){
				future.channel().close();
			}
			try{
				bootstrap.remoteAddress( InetAddress.getByName(getServerAddr()), getServerPort());
				future = bootstrap.connect();
				connectTimes++;

				future.addListener(new ChannelFutureListener() {
					public void operationComplete(ChannelFuture f) throws Exception {
						if (f.isSuccess()) {
							logger.info("Started Tcp Client: " + getServerAddr() + ":" + getServerPort());
							setConnected(true);
						}
						synchronized (notifyObj) {
							notifyObj.notifyAll();
						}
					}
				});
				synchronized (notifyObj) {
					notifyObj.wait();
				}
			}catch( Exception ex ){
				logger.error(ex.getMessage(),ex);
			}
			try{
				if(!isConnected()){
					Thread.sleep(5000);
				}
			}catch(InterruptedException e){
				logger.error(e.getMessage(),e);
			}
		}
	}

	@Override
	public void loginSuccess() {
		super.loginSuccess();
		connectTimes = 0;
	}

	@Override
	public void terminated() {		
		super.terminated();
		close();
	}

	@Override
	public void close() {
		logger.info(this.getGateWayName()+" close!");
		closed = true;
//		setLoginFlag(false);
		future.channel().close();
		group.shutdownGracefully();
		bootstrap = null;
	}

	@Override
	public boolean needConnect() {
		if(isConnected()){
			return false;
		}
		//login失败不再建立连接
		if(isLoginFail()){
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public void init(Map<String, String> gatewayInfo) {
		super.init(gatewayInfo);
	}
}