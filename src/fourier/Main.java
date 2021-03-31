package fourier;

import processing.core.*;
import java.util.ArrayList;
import java.lang.Math;
import java.util.Date;

public class Main extends PApplet{
	public double time = 0;
	
	public ArrayList<Double> xSignalIn;
	public ArrayList<Double> ySignalIn;
	public ArrayList<Epicycle> fourierX;
	public ArrayList<Epicycle> fourierY;
	public ArrayList<PVector> path;
	
	public boolean ready = false;
	
	public float dftCalcTime = 0;
	public float epicycleRenderTime = 0;
	
  public static void main(String[] args) {
  	PApplet.main(new String[] {"fourier.Main"});
  }
  
  public void settings() {
  	size(600, 600);
  }
  
  public void setup() {
  	path = new ArrayList<PVector>();
  	xSignalIn = new ArrayList<Double>();
  	ySignalIn = new ArrayList<Double>();
  }
  
  public void mouseDragged() {
  	if(!ready) {
  		xSignalIn.add((double)mouseX);
  		ySignalIn.add((double)mouseY);
  	}
  }
  
  public void mouseReleased() {
  	if(!ready) {
  		//center points around zero
  		double avgX = 0;
  		double avgY = 0;
  		for(Double x:xSignalIn)avgX+=x;
  		for(Double y:ySignalIn)avgY+=y;
  		avgX/=xSignalIn.size();
  		avgY/=ySignalIn.size();
  		for(int i=0;i<xSignalIn.size();i++)xSignalIn.set(i, xSignalIn.get(i)-avgX);
  		for(int i=0;i<ySignalIn.size();i++)ySignalIn.set(i, ySignalIn.get(i)-avgY);
  		
  		Date dftStart = new Date();
  	  fourierX = dft(xSignalIn);
  	  fourierY = dft(ySignalIn);
  	  dftCalcTime = new Date().getTime()-dftStart.getTime();
  	  ready = true;
  	}
  }
  
  public void draw() {
  	background(170);
  	
  	push();
  	textSize(16);
  	textAlign(CENTER);
  	text("DFT calculation Time: "+dftCalcTime, width*1/4, height-20);
  	text("Render Time: "+epicycleRenderTime, width*3/4, height-20);
  	pop();
  	
  	translate(width/2, height/2);
  	if(ready) {	
      //find point x
      PVector xvec = drawEpicycles(0, -height/4, 0, fourierX);
      //find point y
      PVector yvec = drawEpicycles(-width/4, 0, PI/2, fourierY);
      //make point with x and y
      PVector vec = new PVector(xvec.x, yvec.y);
    
      //add point to path
      path.add(0, vec);
  	
      //draw lines to point
      push();
      stroke(0, 0, 255);
	    line(xvec.x, xvec.y, vec.x, vec.y);
	    line(yvec.x, yvec.y, vec.x, vec.y);
	    pop();
	  
	    //draw path
	    push();
  	  noFill();
  	  stroke(0, 255, 0);
  	  beginShape();
  	  for(PVector p:path) {
  		  vertex(p.x, p.y);
  	  }
  	  endShape();
  	  pop();
  	
  	  //update time
  	  final double dt = (PI*2)/fourierY.size();
  	  time += dt;
  	}
  	surface.setTitle("Fourier Transform of what you are drawing! ... "+round(frameRate)+"fps");
  }
  
  public PVector drawEpicycles(double x, double y, double rot, ArrayList<Epicycle> epis) {
  	//make new times to see what is more intensive
  	int timeForRender = 0;
  	for(int i=0;i<epis.size();i++) {
  		double px = x;
  		double py = y;
  		
  		Epicycle epi = epis.get(i);
  		double freq = epi.freq;
  		double radius = epi.amp;
  		double phase = epi.phase;
  	  x += radius * Math.cos(freq*time+phase+rot);
  	  y += radius * Math.sin(freq*time+phase+rot);
  	
  	  Date renderStart = new Date();
  	  push();
  	  noFill();
  	  stroke(255, 100);
  	  ellipse((float)px, (float)py, (float)radius*2, (float)radius*2);
  	  stroke(255);
  	  line((float)x, (float)y, (float)px, (float)py);
  	  pop();
  	  timeForRender += (new Date().getTime()-renderStart.getTime());
    }
  	epicycleRenderTime = timeForRender;
  	return new PVector((float)x, (float)y);
  }
  
  public ArrayList<Epicycle> dft(ArrayList<Double> v){
  	ArrayList<Epicycle> epis = new ArrayList<Epicycle>();
  	final int N = v.size();
  	for(int k=0;k<N;k++) {
  		double re = 0;
  		double im = 0;
  		for(int n=0;n<N;n++) {
  			double phi = (2*PI*k*n)/N;
  			re += v.get(n)*Math.cos(phi);
  			im -= v.get(n)*Math.sin(phi);
  		}
  		re /= N;
  		im /= N;
  		double freq = k;
  		double amp = Math.sqrt(re*re+im*im);
  		double phase = Math.atan2(im, re);
  		epis.add(new Epicycle(re, im, freq, amp, phase));
  	}
  	return epis;
  }
}
