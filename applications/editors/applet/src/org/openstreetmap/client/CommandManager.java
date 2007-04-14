package org.openstreetmap.client;

import java.awt.EventQueue;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.openstreetmap.util.Releaseable;

/**
 * This class manages all communication commands to the server.
 * All commands are executed in an own thread, one after an other. All data modifications
 * to the intern data representation are done synchronous in the main thread.
 *
 * @author Imi
 */
public class CommandManager implements Releaseable {

	/**
	 * Listener gets notified about some events of the command manager.
   * NB: Should not acquire any locks (call-back from low-level could lead to
   * order reversal, e.g. commandManager lock then applet lock.
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
	private Object sleeper = new byte[0];  
	
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
	 * Schedule a new command at the end of the queue.
   * Always called from the event thread.
	 */
	public void add(ServerCommand command) {
		synchronized (sleeper) {
			commandQueue.add(command);
			command.preConnectionModifyData();
			sleeper.notifyAll();
		}
	}
  
  /**
   * The queue processing thread.
   */
  private Thread thread;

	/**
	 * Start the command queue manager and start the extra thread.
	 */
	public CommandManager() {
		thread = new Thread(new Runnable(){
			public void run() {
        Thread.currentThread().setName("CommandManager_thr");
        try {
  				while (!stop) {
  					ServerCommand command;
  					synchronized(sleeper) {
  						working = false;
  						if (commandQueue.isEmpty())
  							try {sleeper.wait();} catch (InterruptedException e) {/* NOP e.printStackTrace(); */}
  						working = true;
  						command = (ServerCommand)commandQueue.get(0);
  						commandQueue.remove(0);
  					}
  					execute(command);
  				}
        }
        catch (Exception e) {
          System.err.println("Command manager thread aborted: unhandled exception.");
          e.printStackTrace();
        }
			}
		});
	}
	
  /**
   * Starts queue processor. 
   */
  public void start() {
    thread.start(); // do here so don't have to do it unsafely from within constructor
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

			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					if (finalSucceeded)
						command.postConnectionModifyData();
					else {
            // NB: if multiple edits queued up (server conn bad) user's
            // work wiped at this point 
            // TODO queue for offline editing?  prob not, risks even more lost work...
            // could only do with some backup storage site etc...

            // abort the whole queue - and undo all pending edits.
						synchronized (sleeper) {
							//TODO: Do dependency analysis to only clear out commands 
							//depending on the failed one
							System.out.println("command failed. Abort "+(1+commandQueue.size())+" commands.");
							for (ListIterator it = commandQueue.listIterator(commandQueue.size());it.hasPrevious();) {
								final ServerCommand prev = (ServerCommand)it.previous();
                System.out.println("abort: about to undo modify data.");
								prev.undoModifyData();
								// notify, that the command has been aborted
                System.out.println("abort: command undone");
                notifyCommandFinished(prev);
							}
							commandQueue.clear();
							command.undoModifyData();
						}
					}
          notifyCommandFinished(command);
				}
			});
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
  /**
   * Notifies all listeners that a command has finished.
   * To be called on event thread.
   * 
   * @param command The server command that has finished.
   */
  private void notifyCommandFinished(ServerCommand command) {
    for (Iterator it = listener.iterator(); it.hasNext();)
      ((Listener)it.next()).commandFinished(command);
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
  
  /** Termination flag for thread loop */
  private boolean stop = false;
  
  /* (non-Javadoc)
   * @see org.openstreetmap.util.Releaseable#release()
   */
  synchronized public void release() {
    stop = true;
  }
}
