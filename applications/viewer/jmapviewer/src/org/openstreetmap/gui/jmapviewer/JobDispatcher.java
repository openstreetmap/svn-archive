package org.openstreetmap.gui.jmapviewer;

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.openstreetmap.gui.jmapviewer.interfaces.Job;

/**
 * A generic class that processes a list of {@link Runnable} one-by-one using
 * one or more {@link Thread}-instances.
 * 
 * @author Jan Peter Stotz
 */
public class JobDispatcher {

	protected BlockingQueue<Runnable> jobQueue = new LinkedBlockingQueue<Runnable>();

	JobThread[] threads;

	public JobDispatcher(int threadCound) {
		threads = new JobThread[threadCound];
		for (int i = 0; i < threadCound; i++) {
			threads[i] = new JobThread(i + 1);
		}
	}

	/**
	 * Removes all jobs from the queue that are currently not being processed
	 * and stops those currently being processed.
	 */
	public void cancelOutstandingJobs() {
		jobQueue.clear();
		for (int i = 0; i < threads.length; i++) {
			try {
				Runnable job = threads[i].getJob();
				if ((job != null) && (job instanceof Job))
					((Job) job).stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void addJob(Runnable job) {
		try {
			jobQueue.put(job);
		} catch (InterruptedException e) {
		}
	}

	protected class JobThread extends Thread {

		Runnable job;

		public JobThread(int threadId) {
			super("OSMJobThread " + threadId);
			setDaemon(true);
			job = null;
			start();
		}

		@Override
		public void run() {
			while (!isInterrupted()) {
				try {
					job = jobQueue.take();
				} catch (InterruptedException e1) {
					return;
				}
				try {
					job.run();
					job = null;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * @return the job being executed at the moment or <code>null</code> if
		 *         the thread is idle.
		 */
		public Runnable getJob() {
			return job;
		}

	}

}
