package com.cyberlightning.webserver;


public class ProfileService implements MessageReceiver{

	private static final ProfileService _profileService = new ProfileService();

	private ProfileService() {
		MessageHandler.getInstance().registerReceiver(this);
		
	}
	
	public static ProfileService getInstance() {
		return _profileService;
	}
	
	//@Override
	public void messageReceived(String _msg) {
		
		if (_msg.equals("Next")) {
			//TODO
		}
		
		if (_msg.equals("Previous")) {
			//TODO
		}
		
		
		//MessageHandler.getInstance().sendMessage(_msg);
	}
	
	
	
	
}
