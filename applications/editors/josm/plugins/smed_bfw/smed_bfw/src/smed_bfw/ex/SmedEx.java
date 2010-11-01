package smed_bfw.ex;

import javax.swing.JFrame;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

import smed_bfw.api.IManager;

@Component
public class SmedEx extends JFrame {
	
	IManager manager = null;

	@Activate
	public void start() {
		System.out.println("start");
		init();
	}

	private void init() {
		 if(manager == null) System.out.println("something is wrong");
		 else System.out.println("things alright");
	}


	@Deactivate
	public void stop() {
		System.out.println("stop");
	}

	@Reference
	public void setManager(IManager manager) {
		this.manager = manager;
	}
	
	public static void main(String[] args) {
        new SmedEx().start();
    }
}
