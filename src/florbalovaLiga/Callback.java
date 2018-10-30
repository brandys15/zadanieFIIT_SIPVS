package florbalovaLiga;

import sk.ditec.zep.dsigner.xades.XadesSig;

public class Callback implements sk.ditec.zep.dsigner.xades.XadesSig.Callback{
	private static Callback instance = null;
	private Callback() {}
	public static Callback getInstance() {
		if(instance == null)
			instance = new Callback();
		
		return instance;
	}
	
	@Override
	public void onClose(XadesSig arg0) {
	}
	
}
