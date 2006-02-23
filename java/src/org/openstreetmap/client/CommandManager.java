package org.openstreetmap.client;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.SwingUtilities;

/**
 * This class manages all communication commands to the server.
 * All commands are executed in an own thread, one after an other. All data modifications
 * to the intern data representation are done synchronous in the main thread.
 *
 * @author Imi
 */
public class CommandManager {

	/**
	 * Listener gets notified about some events of the command manager.
	 * @author Imi
	 */
	static public interface Listener {
		void commandFinished(ServerCommand command);
	}
	
	/**
	 * The synchonization object between the executor thread and the main thread.
	 * The executor thread will wait on this object and the main thread will awake the
	 * executor if new command are available.
	 */
	private Object sleeper = "";
	
	/**
	 * The command queue to execute. Elements at front are executed next.
	 * Type is ServerCommand.
	 */
	private List commandQueue = new LinkedList();
	
	/**
	 * Set by the executor thread if it is currently working on an command.
	 */
	private boolean working = false;

	/**
	 * List of all listeners. Type: CommandManagerListener
	 */
	private final Collection listener = new LinkedList();
	
	/**
	 * Shedule a new command at the end of the queue. Called from the main thread.
	 */
	public void add(ServerCommand command) {
		synchronized (sleeper) {
			commandQueue.add(command);
			command.preConnectionModifyData();
			sleeper.notifyAll();
		}
	}

	/**
	 * Start the command queue manager and start the extra thread.
	 */
	public CommandManager() {
		new Thread(new Runnable(){
			public void run() {
				while (true) {
					ServerCommand command;
					synchronized(sleeper) {
						working = false;
						if (commandQueue.isEmpty())
							try {sleeper.wait();} catch (InterruptedException e) {e.printStackTrace();}
						working = true;
						command = (ServerCommand)commandQueue.get(0);
						commandQueue.remove(0);
					}
					execute(command);
				}
			}
		}).start();
	}
	
	/**
	 * Executes the command by calling the different steps within the context of the
	 * different threads. Called from the executor thread. execute blocks until the 
	 * command is executed (or undone, if something failed).
	 * (This function is not private for performance reasons.)
	 *
	 * @param command The server command to execute.
	 */
	void execute(final ServerCommand command) {
		try {
			boolean succeeded;
			try {
				succeeded = command.connectToServer();
			} catch (IOException e) {
				succeeded = false;
			}
			final boolean finalSucceeded = succeeded;

			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					if (finalSucceeded)
						command.postConnectionModifyData();
					else {
						// abort the whole queue.
						synchronized (sleeper) {
							//TODO: Do dependency analysis to only clear out commands 
							//depending on the failed one
							System.out.println("command failed. Abort "+(1+commandQueue.size())+" commands.");
							for (ListIterator it = commandQueue.listIterator(commandQueue.size());it.hasPrevious();) {
								final ServerCommand prev = (ServerCommand)it.previous();
								prev.undoModifyData();
								// notify, that the command has been aborted
								for (Iterator l = listener.iterator(); l.hasNext();)
									((Listener)l.next()).commandFinished(prev);
							}
							commandQueue.clear();
							command.undoModifyData();
						}
					}
					// notify listener, that the command has been executed (or aborted)
					for (Iterator it = listener.iterator(); it.hasNext();)
						((Listener)it.next()).commandFinished(command);
				}
			});
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Return the number of commands to execute
	 */
	public int size() {
		synchronized (sleeper) {
			return commandQueue.size() + (working ? 1 : 0);
		}
	}

	/**
	 * Add a listener to the intern list.
	 * @param listener The listener to add. May not be <code>null</code>.
	 */
	public void addListener(Listener listener) {
		this.listener.add(listener);
	}
}
