package calc;

import toms.plug.ifc.Pluggable;
import toms.plug.ifc.PluginManager;

public class CalcPlugin implements Pluggable {

	private Calculator calc;
	private PluginManager manager;
	private boolean running = false;
	
	public CalcPlugin() {
		this.calc = new Calculator();	
	}
	
	@Override
	public boolean start() {
		this.running = true;
		new Thread(new Runnable() {

			@Override
			public void run() {
				int one = 0;
				int two = 0;
				int res = 0;
				
				while(CalcPlugin.this.running) {
					one = (int)(Math.random() * 1000);
					two = (int)(Math.random() * 1000);
					
					res = CalcPlugin.this.calc.add(one, two);
					
					CalcPlugin.this.manager.showVisualMessage(one + " + " + two + " = " + res);
					
					// sleep a little bit
					try {
						Thread.sleep(res);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
				
			
		}).start();
		
		return true;
	}

	@Override
	public boolean stop() {
		this.manager.showVisualMessage("Calculation stopped");
		this.running = false;
		
		return true;
	}

	@Override
	public void setPluginManager(PluginManager manager) {
		this.manager = manager;
	}

}
