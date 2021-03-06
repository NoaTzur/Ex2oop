package gameClient;

import api.directed_weighted_graph;
import api.edge_data;
import api.geo_location;
import api.node_data;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gameClient.util.Point3D;
import gameClient.util.Range;
import gameClient.util.Range2D;
//import org.junit.platform.commons.util.BlacklistedExceptions;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;


/**
 * This class represents a very simple GUI class to presents the graph of the game, the location of the agents
 * and the pokemons as the game is running.
 * we choose the agents to be the pokemon trainer called "may".
 * therse two types of pokemons : skitty (type -1) and zapdos (type 1)
 * in the down left corner you can see the number of moves the agents are making, the Grade and the time that
 * has left to the end of the game
 */
public class MyFrame extends JFrame{
	private Arena _ar;
	private gameClient.util.Range2Range _w2f;

	private Image dbImage;
	private Graphics dbg;
	private Image img = Toolkit.getDefaultToolkit().getImage("data\\final.jpg");


	//pokemons Icons
	File skittyF = new File("data\\skitty.png");
	private BufferedImage skitty = ImageIO.read(skittyF);
	File zapdosF = new File("data\\zapdos.png");
	private BufferedImage zapdos = ImageIO.read(zapdosF);

	//agent Icon
	File mayAgent = new File("data\\may.png");
	private BufferedImage may = ImageIO.read(mayAgent);

		MyFrame(String a) throws IOException {
		super(a);

		//re-size the screen when user will enlarge and reduce the screen with the mouse
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateFrame();

			}
		});

	}
	//updates the arena
	public void update(Arena ar) {
		this._ar = ar;
		updateFrame();
	}

	private void updateFrame() {
		Range rx = new Range(20,this.getWidth()-20);
		Range ry = new Range(this.getHeight()-10,150);
		Range2D frame = new Range2D(rx,ry);
		directed_weighted_graph g = _ar.getGraph();
		_w2f = Arena.w2f(g,frame);
	}
	public void paint(Graphics g) {
		dbImage = createImage(getWidth(), getHeight());
		dbg = dbImage.getGraphics();
		paintComponent(dbg);
		g.drawImage(dbImage, 0, 0, this);

	}

	public void paintComponent(Graphics g) {
		super.paint(g);
		int w = this.getWidth();
		int h = this.getHeight();
		g.clearRect(0, 0, w, h);
		g.drawImage(img, 0, 0, null);
		drawGraph(g);
		drawPokemons(g);
		drawAgants(g);


		//live screen for scores
		g.setColor(Color.BLACK);
		g.drawRoundRect(20,550,200,100, 30,30);
		Color lightGreen = new Color(145,220,169);
		g.setColor(lightGreen);
		g.fillRoundRect(20,550,200,100, 30,30);
		String[] arrInfo = _ar.getInfo().split(":");

		//moves
		Font tri = new Font("TimesRoman", Font.ITALIC, 18);
		String[] moves = arrInfo[4].split(",");
		g.setFont(tri);
		g.setColor(Color.BLACK);
		g.drawString("Moves: "+moves[0],30,580);

		//Grade
		String[] grade = arrInfo[5].split(",");
		g.setFont(tri);
		g.setColor(Color.BLACK);
		g.drawString("Grade: "+grade[0],30,610);

		//time to end
		long time = _ar.getTimeToEnd()/1000;
		g.setFont(tri);
		g.setColor(Color.BLACK);
		g.drawString("Time to END: "+time+" secs",30,640);

	}
	//draw graph
	private void drawGraph(Graphics g) {
		directed_weighted_graph gg = _ar.getGraph();
		Iterator<node_data> iter = gg.getV().iterator();
		while(iter.hasNext()) {
			node_data n = iter.next();
			g.setColor(Color.white);
			drawNode(n,5,g);
			Iterator<edge_data> itr = gg.getE(n.getKey()).iterator();
			while(itr.hasNext()) {
				edge_data e = itr.next();
				g.setColor(Color.white);
				drawEdge(e, g);
			}
		}
	}
	//added pokemons icons to the GUI
	private void drawPokemons(Graphics g) {
		List<CL_Pokemon> fs = _ar.getPokemons();
		if(fs!=null) {
		Iterator<CL_Pokemon> itr = fs.iterator();
		
		while(itr.hasNext()) {

			CL_Pokemon f = itr.next();
			Point3D c = f.getLocation();
			geo_location fp = new Point3D(0,0,0);
			int r=10;

			if(c!=null){
				fp = this._w2f.world2frame(c);
			}
			if(f.getType()<0) { //pokemon type == -1
				g.drawImage(skitty,(int)fp.x()-r, (int)fp.y()-r, 3*r, 3*r, this);

				}
			else { //pokemon type == 1
				g.drawImage(zapdos,(int)fp.x()-r, (int)fp.y()-r, 3*r, 3*r, this);
				}

			}
		}
	}
	//added agents icons to the GUI and score for each agent
	private void drawAgants(Graphics g) {
		List<CL_Agent> ag = _ar.getAgents();

		int i=0;
		while(ag!=null && i<ag.size()) {
			geo_location c = ag.get(i).getLocation();
			int r=8;
			if(c!=null) {
				geo_location fp = this._w2f.world2frame(c);
				g.drawImage(may, (int)fp.x()-r, (int)fp.y()-r, 6*r, 6*r, this);
				int x=(int)fp.x()-4*r, y=(int)fp.y()-6*r;
				Font score=new Font("SansSerif", Font.BOLD, 12);
				g.setFont(score);
				g.setColor(Color.WHITE);
				String Value= ag.get(i).get_value();
				g.drawString("Score: "+ Value, x-3, y-5);
			}
			i++;
		}
	}
	private void drawNode(node_data n, int r, Graphics g) {
		geo_location pos = n.getLocation();
		geo_location fp = this._w2f.world2frame(pos);
		g.fillOval((int)fp.x()-r, (int)fp.y()-r, 2*r, 2*r);
		g.drawString(""+n.getKey(), (int)fp.x(), (int)fp.y()-4*r);
	}
	private void drawEdge(edge_data e, Graphics g) {
		directed_weighted_graph gg = _ar.getGraph();
		geo_location s = gg.getNode(e.getSrc()).getLocation();
		geo_location d = gg.getNode(e.getDest()).getLocation();
		geo_location s0 = this._w2f.world2frame(s);
		geo_location d0 = this._w2f.world2frame(d);
		g.drawLine((int)s0.x(), (int)s0.y(), (int)d0.x(), (int)d0.y());

	}

}
