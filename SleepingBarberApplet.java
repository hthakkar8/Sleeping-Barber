import java.util.*;
import java.applet.Applet;
import java.awt.*;

/*
	<applet code = "SleepingBarberApplet.class" width = 800 height = 800> </applet>
*/

class Mailbox {

	SleepingBarberApplet sba;
	long duration;
	long lastUpdate;
	LinkedList<Integer> mailQueue = new LinkedList<>();
	int capacity = 4;

	Mailbox(SleepingBarberApplet s) {
		sba = s;
		lastUpdate = 0;
	}

	boolean isEmpty() {
		return (mailQueue.size() == 0);
	}

	boolean isFull() {
		return (mailQueue.size() == capacity);
	}

	synchronized void generateDisplayData(int atDoor) {
		String displayString = "";

		if (!Barber.isBarberSleeping)
			displayString = displayString.concat("0,1");
		else
			displayString = displayString.concat("1,0");

		int i = 1;
		while (i <= mailQueue.size() - atDoor) {
			displayString = displayString.concat(",1");
			i++;
		}
		while (i <= capacity) {
			displayString = displayString.concat(",0");
			i++;
		}

		displayString = displayString.concat(",".concat(String.valueOf(atDoor)));
		System.out.println(displayString);

		SleepingBarberApplet.flagsString = displayString;

		synchronized (sba) {
			System.out.println("Inside update");
			if ((System.currentTimeMillis() - lastUpdate) > 25)
				sba.updateApplet();
			lastUpdate = System.currentTimeMillis();
		}

	}
}

class Barber extends Thread {
	Mailbox mailBox;
	static boolean isBarberSleeping = true;

	Barber(Mailbox mb) {
		mailBox = mb;
	}

	void cutHair() {

		if (!mailBox.isEmpty()) {

			mailBox.mailQueue.removeFirst();

			Barber.isBarberSleeping = false;
			synchronized (this) {
				mailBox.generateDisplayData(0);
			}

			try {
				Thread.sleep(4000);
			} catch (Exception e) {
				e.printStackTrace();
			}

			synchronized (mailBox) {
				mailBox.generateDisplayData(0);
			}

		} else {
			try {
				Barber.isBarberSleeping = true;
				synchronized (mailBox) {
					mailBox.generateDisplayData(0);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void run() {
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException iex) {
			iex.printStackTrace();
		}

		while (true) {
			cutHair();
		}
	}

}

class Customer extends Thread {
	
	Mailbox mailBox;
	Barber barber;
	int customerCount = 0;

	Customer(Mailbox mb, Barber b) {
		mailBox = mb;
		barber = b;
	}

	void waitingInQueue() {

		if (!mailBox.isFull()) {
			if (mailBox.mailQueue.size() == 1) {
				if (Barber.isBarberSleeping && mailBox.mailQueue.size() > 0) {
					Barber.isBarberSleeping = false;
					barber.interrupt();
				}
			}

			synchronized (mailBox) {
				mailBox.generateDisplayData(1);
			}
			
			mailBox.mailQueue.add(++customerCount);
			
			System.out.println("Customer Added");

		} else {
			
			synchronized (this) {
				mailBox.generateDisplayData(1);
			}

			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

	public void run() {

		while (true) {
			
			waitingInQueue();

			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}

public class SleepingBarberApplet extends Applet {

	int barberX = 50, barberY = 100, seatX = 100, seatY = 250, radius = 90, offset = radius + 50, textOffset = 20, xOffset = 30;
	static String flagsString = "0,0,0,0,0,0,0";

	Mailbox mb;
	Barber b;
	Customer c;

	public void init() {

		mb = new Mailbox(this);
		b = new Barber(mb);
		c = new Customer(mb, b);

		b.start();
		c.start();
	}

	public void paint(Graphics g) {

		g.setColor(Color.white);
		g.fillRect(0, 0, 800, 800);
		g.setColor(Color.blue);
		g.drawString(flagsString, 50, 50);
		g.drawString("Sleeping", barberX + xOffset, barberY - textOffset);
		g.drawString("Busy", barberX + offset + xOffset, barberY - textOffset);
		g.drawString("Seat 1", seatX + xOffset, seatY + 5 * textOffset);
		g.drawString("Seat 2", seatX + offset + xOffset, seatY + 5 * textOffset);
		g.drawString("Seat 3", seatX + 2 * offset + xOffset, seatY + 5 * textOffset);
		g.drawString("Door", seatX + 4 * offset + xOffset, barberY - textOffset);
		String flags[] = flagsString.split(",");
		if (flags[0].equals("1")) {
			g.setColor(Color.green);
			g.fillOval(barberX, barberY, radius, radius);
		}

		if ((flags[1].equals("1"))) {
			g.setColor(Color.red);
			g.fillOval(barberX + offset, barberY, radius, radius);
		}

		if ((flags[2].equals("1"))) {
			g.setColor(Color.black);
			g.fillOval(seatX, seatY, radius, radius);
		}

		if ((flags[3].equals("1"))) {
			g.setColor(Color.black);
			g.fillOval(seatX + offset, seatY, radius, radius);
		}

		if ((flags[4].equals("1"))) {
			g.setColor(Color.black);
			g.fillOval(seatX + (2 * offset), seatY, radius, radius);
		}

		if ((flags[6].equals("1"))) {
			g.setColor(Color.pink);
			g.fillOval(seatX + (4 * offset), barberY, radius, radius);
		}

	}

	void updateApplet() {
		repaint();
	}
}

