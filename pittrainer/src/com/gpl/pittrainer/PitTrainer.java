//******************************************************************************
// Falcon 4.0 Allied Force Cockpit Trainer
// http://pittrainer.sourceforge.net
// coder_1024@users.sourceforge.net
//
// This program is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free Software
// Foundation; either version 2 of the License, or (at your option) any later
// version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
// details (http://www.opensource.org/licenses/gpl-license.php).
//
// You should have received a copy of the GNU General Public License along with
// this program; if not, write to the
//    Free Software Foundation, Inc.
//    59 Temple Place, Suite 330
//    Boston, MA 02111-1307 USA
//******************************************************************************
package com.gpl.pittrainer;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class PitTrainer implements Runnable
{
   // application title
   private final static String mAppDesc =
      "Falcon 4.0 Allied Force Pit Trainer (v1.2, 09/13/2005)";

   // used to resource files from the jar file
   private final static String mPitImageBase = "images/pit-";
   private final static String mPitImageSuffix = ".jpg";
   private final static String mImageBase = "images/";
   private final static String mSoundBase = "sounds/";

   // mouse cursor settings
   private String mCursorMoveLeftFile = "mouse-move-left.gif";
   private Point mCursorMoveLeftHotspot = new Point(1,5);
   private String mCursorMoveLeftName = "MoveLeftCursor";
   private String mCursorMoveRightFile = "mouse-move-right.gif";
   private Point mCursorMoveRightHotspot = new Point(17,5);
   private String mCursorMoveRightName = "MoveRightCursor";
   private String mCursorMoveUpFile = "mouse-move-up.gif";
   private Point mCursorMoveUpHotspot = new Point(5,1);
   private String mCursorMoveUpName = "MoveUpCursor";
   private String mCursorMoveDownFile = "mouse-move-down.gif";
   private Point mCursorMoveDownHotspot = new Point(5,17);
   private String mCursorMoveDownName = "MoveDownCursor";
   private String mCursorClickableFile = "mouse-clickable.gif";
   private Point mCursorClickableHotspot = new Point(7,7);
   private String mCursorClickableName = "ClickableCursor";
   private String mCursorNotClickableFile = "mouse-notclickable.gif";
   private Point mCursorNotClickableHotspot = new Point(8,8);
   private String mCursorNotClickableName = "NotClickableCursor";

   // mouse view change distances (when mouse is within this distance
   // of the edge, the cursor changes and a left-click moves the view)
   private final static int mMouseMoveDistX = 30;
   private final static int mMouseMoveDistY = 60;

   // application icon
   private final static String mAppIconFile = "superpitsicon1.jpg";
   private Image mAppIcon;

   // size of the window while loading
   private final static int mLoadingWidth = 600;
   private final static int mLoadingHeight = 55;

   // size of the window once loading has completed
   private final static int mWidth = 1024;
   private final static int mHeight = 768;

   // string values of the keyboard commands
   private final static String mKeypadLeft = "NumPad-4";
   private final static String mKeypadRight = "NumPad-6";
   private final static String mKeypadUp = "NumPad-8";
   private final static String mKeypadDown = "NumPad-2";
   private final static String mKeyLeft = "Left";
   private final static String mKeyRight = "Right";
   private final static String mKeyUp = "Up";
   private final static String mKeyDown = "Down";
   private final static String mKeySpace = "Space";

   // rendering preferences for the cockpit position name
   private final static Color mPosNameColor = Color.WHITE;
   private final static int mPosNameFontSizePts = 24;
   private final static int mPosNameFontStyle = Font.BOLD;
   private final static String mPosNameFontName = "Arial";
   private Font mPosNameFont;
   private final static int mPosNameTextX = 1014;
   private final static int mPosNameTextY = 22;

   // rendering preferences for the tip text
   private final static Color mTipColor = Color.ORANGE;
   private final static int mTipFontSizePts = 24;
   private final static int mTipFontStyle = Font.BOLD;
   private final static String mTipFontName = "Arial";
   private Font mTipFont;
   private final static int mTipTextX = 10;
   private final static int mTipTextY = 22;

   // rendering preferences for the tip sub text
   private final static Color mSubTipColor = Color.WHITE;
   private final static int mSubTipFontSizePts = 16;
   private final static int mSubTipFontStyle = Font.BOLD;
   private final static String mSubTipFontName = "Arial";
   private Font mSubTipFont;
   private final static int mSubTipTextX = 10;
   private final static int mSubTipTextY = 43;

   // file to log diagnostic messages to
   private final static String mLogFile = "PitTrainer_log.txt";

   // cmd line arg which puts the app in dev mode, allowing
   // click-and-drag to create new tool tip area code
   private final static String mDevModeArg = "-devmode";

   // indicates whether or not app is in dev mode
   private boolean mDevMode = false;

   // color of click-and-drag rectangle used in dev mode
   private final static Color mDevModeDragBoxColor = Color.RED;

   // logs events to a logfile
   private static void log(String msg)
   {
      try
      {
         FileWriter fw = new FileWriter(mLogFile,true);
         BufferedWriter bw = new BufferedWriter(fw);

         DateFormat df = DateFormat.getInstance();

         Exception le = new Exception();
         StackTraceElement[] ste = le.getStackTrace();

         String _msg = df.format(new Date())
            + ": " + ste[1] + ": " + msg;

         bw.write(_msg,0,_msg.length());
         bw.newLine();
         bw.flush();
         bw.close();
         fw.close();
      }

      catch (Exception ex)
      {
         System.out.println(ex);
         System.out.println("failed to log to file: " + msg);
      }
   }

   /**
    * provides for display of a figure along with the tip information
    */
   private class Figure implements Cloneable
   {
      // file to load the image from
      private String file;
      // loaded image
      private Image img;

      public Object clone()
      {
         Figure result = new Figure(file.substring(0));

         result.img = img;

         return result;
      }

      public Figure(String file)
      {
         this.file = file;
         img = null;
      }

      public void load() throws Exception
      {
         URL imgURL = getClass().getResource(
            mImageBase +
            file);

         Image i = ImageIO.read(imgURL);
         img = i;
      }

      public void render(Graphics g, int x, int y)
      {
         g.drawImage(img,x,y,Color.BLACK,null);
      }

      public String toString()
      {
         return file + ", " + img;
      }
   }

   // collection of available figures
   private final static int mNumFigures = 52;
   private Figure[] mFigures;

   // indices and file names for each figure
   private final static int mFigureRWRSymbology = 0;
   private final static String mFigureRWRSymbologyFile = "RWRSymbology.jpg";

   private final static int mFigureCOMMChannels = 1;
   private final static String mFigureCOMMChannelsFile = "COMMChannels.jpg";

   private final static int mFigureHUDModes = 2;
   private final static String mFigureHUDModesFile = "HUDModes.jpg";

   private final static int mFigureAPSwitches = 3;
   private final static String mFigureAPSwitchesFile = "APSwitches.jpg";

   private final static int mFigureHydraulicA = 4;
   private final static String mFigureHydraulicAFile = "HydraulicA.jpg";

   private final static int mFigureHydraulicB = 5;
   private final static String mFigureHydraulicBFile = "HydraulicB.jpg";

   private final static int mFigureEWSPrgm = 6;
   private final static String mFigureEWSPrgmFile = "EWSPrgm.jpg";

   private final static int mFigurePFL = 7;
   private final static String mFigurePFLFile = "PFL.jpg";

   private final static int mFigureDEDCNI = 8;
   private final static String mFigureDEDCNIFile = "ded-cni.jpg";

   private final static int mFigureDEDILSTR = 9;
   private final static String mFigureDEDILSTRFile = "ded-ils-tr.jpg";

   private final static int mFigureDEDILSAATR = 10;
   private final static String mFigureDEDILSAATRFile = "ded-ils-aatr.jpg";

   private final static int mFigureDEDALOW = 11;
   private final static String mFigureDEDALOWFile = "ded-alow.jpg";

   private final static int mFigureDEDSTPTMAN = 12;
   private final static String mFigureDEDSTPTMANFile = "ded-stpt-man.jpg";

   private final static int mFigureDEDSTPTAUTO = 13;
   private final static String mFigureDEDSTPTAUTOFile = "ded-stpt-auto.jpg";

   private final static int mFigureDEDCRUSTOS = 14;
   private final static String mFigureDEDCRUSTOSFile = "ded-crus-tos.jpg";

   private final static int mFigureDEDCRUSRNG = 15;
   private final static String mFigureDEDCRUSRNGFile = "ded-crus-rng.jpg";

   private final static int mFigureDEDCRUSHOME = 16;
   private final static String mFigureDEDCRUSHOMEFile = "ded-crus-home.jpg";

   private final static int mFigureDEDCRUSCDR = 17;
   private final static String mFigureDEDCRUSCDRFile = "ded-crus-cdr.jpg";

   private final static int mFigureDEDTIME = 18;
   private final static String mFigureDEDTIMEFile = "ded-time.jpg";

   private final static int mFigureDEDMARK = 19;
   private final static String mFigureDEDMARKFile = "ded-mark.jpg";

   private final static int mFigureDEDFIX = 20;
   private final static String mFigureDEDFIXFile = "ded-fix.jpg";

   private final static int mFigureDEDACAL = 21;
   private final static String mFigureDEDACALFile = "ded-acal.jpg";

   private final static int mFigureDEDCOMM1 = 22;
   private final static String mFigureDEDCOMM1File = "ded-comm1.jpg";

   private final static int mFigureDEDCOMM2 = 23;
   private final static String mFigureDEDCOMM2File = "ded-comm2.jpg";

   private final static int mFigureDEDIFF = 24;
   private final static String mFigureDEDIFFFile = "ded-iff.jpg";

   private final static int mFigureDEDLIST = 25;
   private final static String mFigureDEDLISTFile = "ded-list.jpg";

   private final static int mFigureDEDLISTDESTDIR = 26;
   private final static String mFigureDEDLISTDESTDIRFile = "ded-list-dest-dir.jpg";

   private final static int mFigureDEDLISTDESTOA1 = 27;
   private final static String mFigureDEDLISTDESTOA1File = "ded-list-dest-oa1.jpg";

   private final static int mFigureDEDLISTDESTOA2 = 28;
   private final static String mFigureDEDLISTDESTOA2File = "ded-list-dest-oa2.jpg";

   private final static int mFigureDEDLISTBINGO = 29;
   private final static String mFigureDEDLISTBINGOFile = "ded-list-bingo.jpg";

   private final static int mFigureDEDLISTVIP = 30;
   private final static String mFigureDEDLISTVIPFile = "ded-list-vip.jpg";

   private final static int mFigureDEDLISTINTG = 31;
   private final static String mFigureDEDLISTINTGFile = "ded-list-intg.jpg";

   private final static int mFigureDEDLISTDLINK = 32;
   private final static String mFigureDEDLISTDLINKFile = "ded-list-dlink.jpg";

   private final static int mFigureDEDLISTMISC = 33;
   private final static String mFigureDEDLISTMISCFile = "ded-list-misc.jpg";

   private final static int mFigureDEDLISTMISCCORR = 34;
   private final static String mFigureDEDLISTMISCCORRFile = "ded-list-misc-corr.jpg";

   private final static int mFigureDEDLISTMISCMAGV = 35;
   private final static String mFigureDEDLISTMISCMAGVFile = "ded-list-misc-magv.jpg";

   private final static int mFigureDEDLISTMISCOFP = 36;
   private final static String mFigureDEDLISTMISCOFPFile = "ded-list-misc-ofp.jpg";

   private final static int mFigureDEDLISTMISCINSM = 37;
   private final static String mFigureDEDLISTMISCINSMFile = "ded-list-misc-insm.jpg";

   private final static int mFigureDEDLISTMISCLASR = 38;
   private final static String mFigureDEDLISTMISCLASRFile = "ded-list-misc-lasr.jpg";

   private final static int mFigureDEDLISTMISCGPS = 39;
   private final static String mFigureDEDLISTMISCGPSFile = "ded-list-misc-gps.jpg";

   private final static int mFigureDEDLISTMISCDRNG = 40;
   private final static String mFigureDEDLISTMISCDRNGFile = "ded-list-misc-drng.jpg";

   private final static int mFigureDEDLISTMISCBULL = 41;
   private final static String mFigureDEDLISTMISCBULLFile = "ded-list-misc-bull.jpg";

   private final static int mFigureDEDLISTMISCWPT = 42;
   private final static String mFigureDEDLISTMISCWPTFile = "ded-list-misc-tgttowpt.jpg";

   private final static int mFigureDEDLISTNAV = 43;
   private final static String mFigureDEDLISTNAVFile = "ded-list-nav.jpg";

   private final static int mFigureDEDLISTMAN = 44;
   private final static String mFigureDEDLISTMANFile = "ded-list-man.jpg";

   private final static int mFigureDEDLISTINS = 45;
   private final static String mFigureDEDLISTINSFile = "ded-list-ins.jpg";

   private final static int mFigureDEDLISTEWS = 46;
   private final static String mFigureDEDLISTEWSFile = "ded-list-ews.jpg";

   private final static int mFigureDEDLISTMODEAA = 47;
   private final static String mFigureDEDLISTMODEAAFile = "ded-list-mode-aa.jpg";

   private final static int mFigureDEDLISTMODEAG = 48;
   private final static String mFigureDEDLISTMODEAGFile = "ded-list-mode-ag.jpg";

   private final static int mFigureDEDLISTVRP = 49;
   private final static String mFigureDEDLISTVRPFile = "ded-list-vrp.jpg";

   private final static int mFigureDEDCNIWIND = 50;
   private final static String mFigureDEDCNIWINDFile = "ded-cniwind.jpg";

   private final static int mFigureThreatWingspans= 51;
   private final static String mFigureThreatWingspansFile = "ThreatWingspans.jpg";

   /**
    * represents an area of the screen along with textual tip information and
    * optionally a supporting figure.  the area can be described using a
    * rectangle (upper left/lower right), circle (center/radius), or polygon
    * (series of points)
    */
   private class TipArea
   {
      private int ulX;
      private int ulY;
      private int lrX;
      private int lrY;
      private Polygon poly;
      private String tipText;
      private String tipSubText;
      private Figure figure;
      private int figureX;
      private int figureY;
      private int cX;
      private int cY;
      private int radius;
      private PitEvent event;

      // creates a clone of this tip area at a specified positional offset
      public TipArea clone(int offsetX, int offsetY)
      {
         int _ulX = ulX;
         int _ulY = ulY;
         int _lrX = lrX;
         int _lrY = lrY;

         if ((poly == null) && (radius == -1))
         {
            _ulX += offsetX;
            _ulY += offsetY;
            _lrX += offsetX;
            _lrY += offsetY;
         }

         TipArea result = new TipArea(_ulX,_ulY,_lrX,_lrY,tipText.substring(0),tipSubText.substring(0));

         if (poly != null)
         {
            result.poly = new Polygon();
            PathIterator pi = poly.getPathIterator(new AffineTransform());
            double [] polyData = new double[6];
            while (!pi.isDone())
            {
               pi.currentSegment(polyData);
               result.poly.addPoint((int)polyData[0],(int)polyData[1]);
               pi.next();
            }

            result.poly.translate(offsetX,offsetY);
         }
         else
         {
            result.poly = null;
         }

         // deep copy is not performed on the figure, since the copy wouldn't get loaded
         // during resource loading.
         result.figure = figure;

         result.figureX = figureX;
         result.figureY = figureY;

         int _cX = cX;
         int _cY = cY;
         int _radius = radius;

         if (radius != -1)
         {
            _cX += offsetX;
            _cY += offsetY;
         }

         result.cX = _cX;
         result.cY = _cY;
         result.radius = _radius;

         result.event = event;

         return result;
      }

      public TipArea(int ulX, int ulY, int lrX, int lrY, String tipText, String tipSubText)
      {
         this.ulX = ulX;
         this.ulY = ulY;
         this.lrX = lrX;
         this.lrY = lrY;
         this.poly = null;
         this.tipText = tipText;
         this.tipSubText = tipSubText;
         this.figure = null;
         this.figureX = -1;
         this.figureY = -1;
         this.cX = -1;
         this.cY = -1;
         this.radius = -1;
         event = null;
      }

      public TipArea(Polygon poly, String tipText, String tipSubText)
      {
         ulX = -1;
         ulY = -1;
         lrX = -1;
         lrY = -1;
         this.poly = poly;
         this.tipText = tipText;
         this.tipSubText = tipSubText;
         this.figure = null;
         this.figureX = -1;
         this.figureY = -1;
         this.cX = -1;
         this.cY = -1;
         this.radius = -1;
         event = null;
      }

      public TipArea(int ulX, int ulY, int lrX, int lrY, String tipText, String tipSubText, Figure figure, int figureX, int figureY)
      {
         this.ulX = ulX;
         this.ulY = ulY;
         this.lrX = lrX;
         this.lrY = lrY;
         this.poly = null;
         this.tipText = tipText;
         this.tipSubText = tipSubText;
         this.figure = figure;
         this.figureX = figureX;
         this.figureY = figureY;
         this.cX = -1;
         this.cY = -1;
         this.radius = -1;
         event = null;
      }

      public TipArea(Polygon poly, String tipText, String tipSubText, Figure figure, int figureX, int figureY)
      {
         ulX = -1;
         ulY = -1;
         lrX = -1;
         lrY = -1;
         this.poly = poly;
         this.tipText = tipText;
         this.tipSubText = tipSubText;
         this.figure = figure;
         this.figureX = figureX;
         this.figureY = figureY;
         this.cX = -1;
         this.cY = -1;
         this.radius = -1;
         event = null;
      }

      public TipArea(int cX, int cY, int radius, String tipText, String tipSubText)
      {
         ulX = -1;
         ulY = -1;
         lrX = -1;
         lrY = -1;
         this.poly = null;
         this.tipText = tipText;
         this.tipSubText = tipSubText;
         this.figure = null;
         this.figureX = -1;
         this.figureY = -1;
         this.cX = cX;
         this.cY = cY;
         this.radius = radius;
         event = null;
      }

      public TipArea(int cX, int cY, int radius, String tipText, String tipSubText, Figure figure, int figureX, int figureY)
      {
         ulX = -1;
         ulY = -1;
         lrX = -1;
         lrY = -1;
         this.poly = null;
         this.tipText = tipText;
         this.tipSubText = tipSubText;
         this.figure = figure;
         this.figureX = figureX;
         this.figureY = figureY;
         this.cX = cX;
         this.cY = cY;
         this.radius = radius;
         event = null;
      }

      /**
       * does the specified point fall within this tip area?
       */
      public boolean pointInArea(int x, int y)
      {
         boolean result = false;

         if (poly != null)
         {
            result = poly.contains(x,y);
         }
         else if (radius != -1)
         {
            double dx = x - cX;
            double dy = y - cY;
            double dist = Math.sqrt((dx*dx)+(dy*dy));

            if ((int)dist <= radius)
            {
               result = true;
            }
         }
         else
         {
            if ((x >= ulX) &&
                (x <= lrX) &&
                (y >= ulY) &&
                (y <= lrY))
            {
               result = true;
            }
         }

         return result;
      }

      public void setEvent(PitEvent event)
      {
         this.event = event;
      }

      public PitEvent getEvent()
      {
         return event;
      }

      /**
       * render this tip area along with its figure if there is one
       */
      public void render(Graphics g)
      {
         // draw tip outline
         g.setColor(mTipColor);
         if (poly != null)
         {
            g.drawPolygon(poly);
         }
         else if (radius != -1)
         {
            g.drawOval(cX-radius,cY-radius,radius*2,radius*2);
         }
         else
         {
            g.drawRect(ulX,ulY,lrX-ulX,lrY-ulY);
         }

         // draw tip text
         g.setFont(mTipFont);
         g.setColor(Color.BLACK);
         g.drawString(tipText,mTipTextX+1,mTipTextY+1);
         g.drawString(tipText,mTipTextX+1,mTipTextY-1);
         g.drawString(tipText,mTipTextX-1,mTipTextY-1);
         g.drawString(tipText,mTipTextX-1,mTipTextY+1);
         g.setColor(mTipColor);
         g.drawString(tipText,mTipTextX,mTipTextY);

         // draw tip sub text
         g.setFont(mSubTipFont);
         g.setColor(Color.BLACK);
         g.drawString(tipSubText,mSubTipTextX+1,mSubTipTextY+1);
         g.drawString(tipSubText,mSubTipTextX+1,mSubTipTextY-1);
         g.drawString(tipSubText,mSubTipTextX-1,mSubTipTextY-1);
         g.drawString(tipSubText,mSubTipTextX-1,mSubTipTextY+1);
         g.setColor(mSubTipColor);
         g.drawString(tipSubText,mSubTipTextX,mSubTipTextY);

         // draw figure if there is one
         if (figure != null)
         {
            figure.render(g,figureX,figureY);
         }
      }
   }

   /// the generic tip area shown when the mouse is not over any other tip area
   private TipArea mGenericTipArea;

   /**
    * Cockpit events
    */
   private static class PitEvent
   {
      private String eventName;

      private PitEvent(String eventName)
      {
         this.eventName = eventName;
      }

      public int hashCode()
      {
         return eventName.hashCode();
      }

      public boolean equals(Object obj)
      {
         boolean result = false;

         if (obj instanceof PitEvent)
         {
            if (eventName.compareTo(((PitEvent)obj).eventName) == 0)
            {
               result = true;
            }
         }

         return result;
      }

      public String toString()
      {
         return eventName;
      }

      public static final PitEvent ICP_ILS = new PitEvent("ICP_ILS");
      public static final PitEvent ICP_ALOW = new PitEvent("ICP_ALOW");
      public static final PitEvent ICP_3 = new PitEvent("ICP_3");
      public static final PitEvent ICP_RCL = new PitEvent("ICP_RCL");
      public static final PitEvent ICP_STPT = new PitEvent("ICP_STPT");
      public static final PitEvent ICP_CRUS = new PitEvent("ICP_CRUS");
      public static final PitEvent ICP_TIME = new PitEvent("ICP_TIME");
      public static final PitEvent ICP_ENTR = new PitEvent("ICP_ENTR");
      public static final PitEvent ICP_MARK = new PitEvent("ICP_MARK");
      public static final PitEvent ICP_FIX = new PitEvent("ICP_FIX");
      public static final PitEvent ICP_ACAL = new PitEvent("ICP_ACAL");
      public static final PitEvent ICP_MSEL = new PitEvent("ICP_MSEL");
      public static final PitEvent ICP_RTN = new PitEvent("ICP_RTN");
      public static final PitEvent ICP_SEQ = new PitEvent("ICP_SEQ");
      public static final PitEvent ICP_COMM1 = new PitEvent("ICP_COMM1");
      public static final PitEvent ICP_COMM2 = new PitEvent("ICP_COMM2");
      public static final PitEvent ICP_IFF = new PitEvent("ICP_IFF");
      public static final PitEvent ICP_LIST = new PitEvent("ICP_LIST");
   }

   /**
    * a single state in an ActivePanel
    */
   private class APState
   {
      private Figure figure;
      // key:event, value:new state
      private Hashtable transitions;
      private Vector tipAreas;
      private boolean rcheck;
      private APState rcheckState;

      public Object clone(int offsetX,int offsetY)
      {
         APState result = new APState(figure);

         if (!rcheck)
         {
            for (Enumeration e = transitions.keys(); e.hasMoreElements(); )
            {
               Object obj = e.nextElement();
               APState newState = (APState)transitions.get(obj);
               rcheck = true;
               rcheckState = this;
               APState clonedState = (APState)newState.clone(offsetX,offsetY);
               rcheck = false;
               result.transitions.put(obj,clonedState);
            }
         }
         else
         {
            result.transitions = transitions;
         }

         for (int x = 0; x < tipAreas.size(); x++)
         {
            result.tipAreas.add(((TipArea)tipAreas.get(x)).clone(offsetX,offsetY));
         }

         return result;
      }

      public void copyTipAreas(APState other)
      {
         for (int x = 0; x < other.tipAreas.size(); x++)
         {
            tipAreas.add(((TipArea)other.tipAreas.get(x)).clone(0,0));
         }
      }

      public APState(Figure figure)
      {
         this.figure = figure;
         transitions = new Hashtable();
         tipAreas = new Vector();
         rcheck = false;
      }

      public APState transition(PitEvent event)
      {
         APState result = this;

         APState newState = (APState)transitions.get(event);
         if (newState != null)
         {
            result = newState;
         }

         return result;
      }

      public void addTransition(PitEvent event, APState newState)
      {
         transitions.put(event,newState);
      }

      public void addTipArea(TipArea tipArea)
      {
         tipAreas.add(tipArea);
      }

      public TipArea findTipArea(int x, int y)
      {
         TipArea result = null;

         for (int i = 0; (i < tipAreas.size()) && (result == null); i++)
         {
            TipArea tipArea = (TipArea)tipAreas.get(i);
            if (tipArea.pointInArea(x,y))
            {
               result = tipArea;
            }
         }

         return result;
      }

      public void render(Graphics g, int x, int y)
      {
         figure.render(g,x,y);
      }
   }

   /**
    * represents multi-state sub-displays rendered for particular cockpit
    * positions used to provide interactive displays.
    */
   private class ActivePanel
   {
      private int x;
      private int y;
      private APState currentState;
      private Hashtable overrides;

      public Object clone(int offsetX, int offsetY)
      {
         ActivePanel result = new ActivePanel(x+offsetX,y+offsetY);
         if (currentState != null)
         {
            result.currentState = (APState)currentState.clone(offsetX,offsetY);
         }
         else
         {
            result.currentState = null;
         }

         for (Enumeration e = overrides.keys(); e.hasMoreElements(); )
         {
            Object obj = e.nextElement();
            result.overrides.put(obj,((APState)overrides.get(obj)).clone(offsetX,offsetY));
         }

         return result;
      }

      public ActivePanel(int x, int y)
      {
         this.x = x;
         this.y = y;
         currentState = null;
         overrides = new Hashtable();
      }

      public void setCurrentState(APState state)
      {
         currentState = state;
      }

      public void transition(PitEvent event)
      {
         if (currentState != null)
         {
            setCurrentState(currentState.transition(event));
         }
      }

      public void addOverride(PitEvent event, APState state)
      {
         overrides.put(event,state);
      }

      public void render(Graphics g)
      {
         if (currentState != null)
         {
            currentState.render(g,x,y);
         }
      }

      public void handleEvent(PitEvent event)
      {
         APState os = (APState)overrides.get(event);
         if (os != null)
         {
            currentState = os;
         }
         else if (currentState != null)
         {
            currentState = currentState.transition(event);
         }
      }

      public TipArea findTipArea(int x, int y)
      {
         TipArea result = null;

         if (currentState != null)
         {
            result = currentState.findTipArea(x,y);
         }

         return result;
      }
   }

   /**
    * represents a cockpit view position.  this includes an image of the cockpit
    * at the view position, and indices for the other positions to switch to when
    * moving away from this position in the left, right, up, and down directions.
    * optionally, it can also include a name for the position.
    */
   private class Position
   {
      private int idx;
      private int left;
      private int right;
      private int up;
      private int down;
      private Image img;
      private Vector tipAreas;
      private String name;
      private Vector activePanels;

      public String toString()
      {
         return Integer.toString(idx) + ", " + name;
      }

      public Position(int idx, int left, int right, int up, int down)
      {
         this.idx = idx;
         this.left = left;
         this.right = right;
         this.up = up;
         this.down = down;
         img = null;
         tipAreas = new Vector();
         this.name = "";
         activePanels = new Vector();
      }

      public Position(int idx, int left, int right, int up, int down, String name)
      {
         this.idx = idx;
         this.left = left;
         this.right = right;
         this.up = up;
         this.down = down;
         img = null;
         tipAreas = new Vector();
         this.name = name;
         activePanels = new Vector();
      }

      /**
       * adds a tip area to this position
       */
      public void addTipArea(TipArea tipArea)
      {
         tipAreas.add(tipArea);
      }

      /**
       * adds an active panel to this position
       */
      public void addActivePanel(ActivePanel activePanel)
      {
         activePanels.add(activePanel);
      }

      /**
       * handle a pit event
       */
      public void handleEvent(PitEvent event)
      {
         for (int x = 0; x < activePanels.size(); x++)
         {
            ((ActivePanel)activePanels.get(x)).handleEvent(event);
         }
      }

      /**
       * find the tip area in which the specified point lies if there is one
       */
      public TipArea findTipArea(int x, int y)
      {
         TipArea result = null;

         for (int j = 0; (j < activePanels.size()) && (result == null); j++)
         {
            result = ((ActivePanel)activePanels.get(j)).findTipArea(x,y);
         }

         for (int i = 0; (i < tipAreas.size()) && (result == null); i++)
         {
            TipArea tipArea = (TipArea)tipAreas.get(i);
            if (tipArea.pointInArea(x,y))
            {
               result = tipArea;
            }
         }

         return result;
      }

      /**
       * make a duplicate of all contained tip areas at the specified offset
       */
      public Vector cloneTipAreas(int offsetX, int offsetY)
      {
         Vector result = new Vector();
         for (int x = 0; x < tipAreas.size(); x++)
         {
            result.add(((TipArea)tipAreas.get(x)).clone(offsetX,offsetY));
         }
         return result;
      }

      public Vector cloneActivePanels(int offsetX, int offsetY)
      {
         Vector result = new Vector();
         for (int x = 0; x < activePanels.size(); x++)
         {
            result.add(((ActivePanel)activePanels.get(x)).clone(offsetX,offsetY));
         }
         return result;
      }

      public String getName()
      {
         return name;
      }

      public int getIdx()
      {
         return idx;
      }

      public Image getImage()
      {
         return img;
      }

      public void load() throws Exception
      {
         URL imgURL = getClass().getResource(
            mPitImageBase +
            Integer.toString(getIdx()) +
            mPitImageSuffix);

         Image i = ImageIO.read(imgURL);
         img = i;
      }

      public int getLeft()
      {
         return left;
      }

      public int getRight()
      {
         return right;
      }

      public int getUp()
      {
         return up;
      }

      public int getDown()
      {
         return down;
      }

      public void render(Graphics g, int width, int height)
      {
         // background image
         Image img = getImage();
         g.drawImage(img,0,0,Color.BLACK,null);

         // pit position name
         String posName = getName();
         g.setFont(mPosNameFont);
         Rectangle2D textRect = g.getFontMetrics().getStringBounds(posName,g);
         g.setColor(Color.BLACK);
         g.drawString(posName,width-10-(int)textRect.getWidth()+1,mPosNameTextY+1);
         g.drawString(posName,width-10-(int)textRect.getWidth()+1,mPosNameTextY-1);
         g.drawString(posName,width-10-(int)textRect.getWidth()-1,mPosNameTextY-1);
         g.drawString(posName,width-10-(int)textRect.getWidth()-1,mPosNameTextY+1);
         g.setColor(mPosNameColor);
         g.drawString(posName,width-10-(int)textRect.getWidth(),mPosNameTextY);

         // active panels
         for (int x = 0; x < activePanels.size(); x++)
         {
            ((ActivePanel)activePanels.get(x)).render(g);
         }
      }
   }

   // the set of available positions
   private final static int mNumPositions = 52;
   private Position[] mPositions = null;

   // position index to start at when first loaded
   private final static int mStartPosition = 0;

   // current position index
   private int mCurrentPosition = mStartPosition;

   // currently applicable tip area based on mouse position
   // NULL if the mouse is not over any tip area
   private TipArea mCurrentTipArea = null;

   /**
    * represents a sound effect.  includes the file to load the effect from and
    * the effect itself.
    */
   private class SoundEffect
   {
      private String file;
      private AudioClip audioClip;

      public SoundEffect(String file)
      {
         this.file = file;
         this.audioClip = null;
      }

      public void load() throws Exception
      {
         URL sndURL = getClass().getResource(
            mSoundBase +
            file);

         audioClip = Applet.newAudioClip(sndURL);
      }

      public void play()
      {
         audioClip.play();
      }

      public String toString()
      {
         return file;
      }
   }

   // the set of available sound effects
   private final static int mNumSoundEffects = 2;
   private SoundEffect[] mSoundEffects = null;

   // indices and file names for each sound
   private final static int mSoundChangeView = 0;
   private final static String mSoundChangeViewFile = "chngview.wav";

   private final static int mSoundICPClick = 1;
   private final static String mSoundICPClickFile = "ICP1.wav";

   // Swing GUI objects
   private JFrame mMainFrame;
   private JProgressBar mLoadProgress;
   private PitPanel mPitPanel;
   private PitPanelUpdater mPitPanelUpdater;
   private PitKeyListener mPitKeyListener;
   private PitMouseListener mPitMouseListener;

   // alternate mouse cursors for view changing
   private Cursor mCursorMoveLeft;
   private Cursor mCursorMoveRight;
   private Cursor mCursorMoveUp;
   private Cursor mCursorMoveDown;
   private Cursor mCursorClickable;
   private Cursor mCursorNotClickable;
   private Cursor mCursorDefault;

   /**
    * app initialization (before loading of resources occurs)
    */
   public void init()
   {
      try
      {
         //************************************************************************
         // Swing GUI initialization
         //************************************************************************

         // enable developer mode if appropriate
         if (mDevMode)
         {
            mMainFrame = new JFrame(mAppDesc + " " + mDevModeArg);
         }
         else
         {
            mMainFrame = new JFrame(mAppDesc);
         }

         // initialize frame components
         mMainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         mLoadProgress = new JProgressBar(0,mNumPositions + mNumSoundEffects + mNumFigures);
         mLoadProgress.setString("Loading...");
         mLoadProgress.setValue(0);
         mLoadProgress.setStringPainted(true);
         mMainFrame.getContentPane().add(mLoadProgress);

         // set application icon
         URL url = getClass().getResource(
            mImageBase + mAppIconFile);
         mAppIcon = ImageIO.read(url);
         mMainFrame.setIconImage(mAppIcon);

         // load cursors
         Toolkit defTK = Toolkit.getDefaultToolkit();

         mCursorMoveLeft = defTK.createCustomCursor(
            defTK.getImage(getClass().getResource(mImageBase + mCursorMoveLeftFile)),
            mCursorMoveLeftHotspot,mCursorMoveLeftName);

         mCursorMoveRight = defTK.createCustomCursor(
            defTK.getImage(getClass().getResource(mImageBase + mCursorMoveRightFile)),
            mCursorMoveRightHotspot,mCursorMoveRightName);

         mCursorMoveUp = defTK.createCustomCursor(
            defTK.getImage(getClass().getResource(mImageBase + mCursorMoveUpFile)),
            mCursorMoveUpHotspot,mCursorMoveUpName);

         mCursorMoveDown = defTK.createCustomCursor(
            defTK.getImage(getClass().getResource(mImageBase + mCursorMoveDownFile)),
            mCursorMoveDownHotspot,mCursorMoveDownName);

         mCursorClickable = defTK.createCustomCursor(
            defTK.getImage(getClass().getResource(mImageBase + mCursorClickableFile)),
            mCursorClickableHotspot,mCursorClickableName);

         mCursorNotClickable = defTK.createCustomCursor(
            defTK.getImage(getClass().getResource(mImageBase + mCursorNotClickableFile)),
            mCursorNotClickableHotspot,mCursorNotClickableName);

         mCursorDefault = mCursorNotClickable;
         mMainFrame.setCursor(mCursorDefault);

         // show main window
         mMainFrame.pack();
         mMainFrame.setSize(mLoadingWidth,mLoadingHeight);
         mMainFrame.setVisible(true);

         //************************************************************************
         // cockpit positions
         //************************************************************************

         //
         // provides images for each position and
         // directs motion around the pit.  orientation
         // of the pit positions wrt each other is
         // shown below:
         //
         // 51 50 49 48 43 44 45 46 47
         // 42 41 40 39 34 35 36 37 38
         // 33 32 31 30 25 26 27 28 29
         // 12 11 10 09 08 07 06 05 04
         //    13 14 15 00 01 02 03
         //    22 21 20 16 17 18 19
         //             23
         //             24
         //
         // each pit position is initialized with the
         // positions in the left, right, up, down
         // directions from the pit, allowing navigation
         // around the various cockpit images.  some of the
         // positions are also initialized with names which
         // match the manual.
         //
         // Note that of the 52 positions provided, only
         // 9 of them actually have controls for which help
         // is provided for by this application.  The rest
         // are included to provide a more immersive feel,
         // i.e. making it feel just like it would in-game.
         mPositions = new Position[mNumPositions];
         mPositions[0]  = new Position(0,15,1,8,16,"Instrument Panel (D)");
         mPositions[1]  = new Position(1,0,2,7,17);
         mPositions[2]  = new Position(2,1,3,6,18);
         mPositions[3]  = new Position(3,2,4,5,19);
         mPositions[4]  = new Position(4,5,4,29,3);
         mPositions[5]  = new Position(5,6,4,28,3);
         mPositions[6]  = new Position(6,7,5,27,2);
         mPositions[7]  = new Position(7,8,6,26,1);
         mPositions[8]  = new Position(8,9,7,25,0);
         mPositions[9]  = new Position(9,10,8,30,15);
         mPositions[10] = new Position(10,11,9,31,14);
         mPositions[11] = new Position(11,12,10,32,13);
         mPositions[12] = new Position(12,12,11,32,13);
         mPositions[13] = new Position(13,12,14,11,22);
         mPositions[14] = new Position(14,13,15,10,21);
         mPositions[15] = new Position(15,14,0,9,20);
         mPositions[16] = new Position(16,20,17,0,23,"Instrument Panel (E)");
         mPositions[17] = new Position(17,16,18,1,17,"Right Aux Console (F)");
         mPositions[18] = new Position(18,17,19,2,18,"Right Console (G)");
         mPositions[19] = new Position(19,18,3,3,19,"Right Console (H)");
         mPositions[20] = new Position(20,21,16,15,20,"Left Aux Console (C)");
         mPositions[21] = new Position(21,22,20,14,21,"Left Console (B)");
         mPositions[22] = new Position(22,13,21,13,22,"Left Console (A)");
         mPositions[23] = new Position(23,20,17,16,24,"Instrument Panel (E)");
         mPositions[24] = new Position(24,24,24,0,0);
         mPositions[25] = new Position(25,30,26,34,8);
         mPositions[26] = new Position(26,25,27,35,7);
         mPositions[27] = new Position(27,26,28,36,6);
         mPositions[28] = new Position(28,27,29,37,5);
         mPositions[29] = new Position(29,28,29,38,4);
         mPositions[30] = new Position(30,31,25,39,9);
         mPositions[31] = new Position(31,32,30,40,10);
         mPositions[32] = new Position(32,33,31,41,11);
         mPositions[33] = new Position(33,33,32,42,12);
         mPositions[34] = new Position(34,39,35,43,25);
         mPositions[35] = new Position(35,34,36,44,26);
         mPositions[36] = new Position(36,35,37,45,27);
         mPositions[37] = new Position(37,36,38,46,28);
         mPositions[38] = new Position(38,37,38,47,29);
         mPositions[39] = new Position(39,40,34,48,30);
         mPositions[40] = new Position(40,41,39,49,31);
         mPositions[41] = new Position(41,42,40,50,32);
         mPositions[42] = new Position(42,42,41,51,33);
         mPositions[43] = new Position(43,48,44,43,34);
         mPositions[44] = new Position(44,43,45,44,35);
         mPositions[45] = new Position(45,44,46,45,36);
         mPositions[46] = new Position(46,45,47,46,37);
         mPositions[47] = new Position(47,46,47,47,38);
         mPositions[48] = new Position(48,49,43,48,39);
         mPositions[49] = new Position(49,50,48,48,40);
         mPositions[50] = new Position(50,51,49,50,41);
         mPositions[51] = new Position(51,51,49,51,42);

         Polygon poly = null;

         //************************************************************************
         // figures
         //************************************************************************

         mFigures = new Figure[mNumFigures];
         mFigures[mFigureRWRSymbology] = new Figure(mFigureRWRSymbologyFile);
         mFigures[mFigureCOMMChannels] = new Figure(mFigureCOMMChannelsFile);
         mFigures[mFigureHUDModes] = new Figure(mFigureHUDModesFile);
         mFigures[mFigureAPSwitches] = new Figure(mFigureAPSwitchesFile);
         mFigures[mFigureHydraulicA] = new Figure(mFigureHydraulicAFile);
         mFigures[mFigureHydraulicB] = new Figure(mFigureHydraulicBFile);
         mFigures[mFigureEWSPrgm] = new Figure(mFigureEWSPrgmFile);
         mFigures[mFigurePFL] = new Figure(mFigurePFLFile);

         mFigures[mFigureDEDCNI] = new Figure(mFigureDEDCNIFile);
         mFigures[mFigureDEDCNIWIND] = new Figure(mFigureDEDCNIWINDFile);
         mFigures[mFigureDEDILSTR] = new Figure(mFigureDEDILSTRFile);
         mFigures[mFigureDEDILSAATR] = new Figure(mFigureDEDILSAATRFile);
         mFigures[mFigureDEDALOW] = new Figure(mFigureDEDALOWFile);
         mFigures[mFigureDEDSTPTMAN] = new Figure(mFigureDEDSTPTMANFile);
         mFigures[mFigureDEDSTPTAUTO] = new Figure(mFigureDEDSTPTAUTOFile);
         mFigures[mFigureDEDCRUSTOS] = new Figure(mFigureDEDCRUSTOSFile);
         mFigures[mFigureDEDCRUSRNG] = new Figure(mFigureDEDCRUSRNGFile);
         mFigures[mFigureDEDCRUSHOME] = new Figure(mFigureDEDCRUSHOMEFile);
         mFigures[mFigureDEDCRUSCDR] = new Figure(mFigureDEDCRUSCDRFile);
         mFigures[mFigureDEDTIME] = new Figure(mFigureDEDTIMEFile);
         mFigures[mFigureDEDMARK] = new Figure(mFigureDEDMARKFile);
         mFigures[mFigureDEDFIX] = new Figure(mFigureDEDFIXFile);
         mFigures[mFigureDEDACAL] = new Figure(mFigureDEDACALFile);
         mFigures[mFigureDEDCOMM1] = new Figure(mFigureDEDCOMM1File);
         mFigures[mFigureDEDCOMM2] = new Figure(mFigureDEDCOMM2File);
         mFigures[mFigureDEDIFF] = new Figure(mFigureDEDIFFFile);
         mFigures[mFigureDEDLIST] = new Figure(mFigureDEDLISTFile);
         mFigures[mFigureDEDLISTDESTDIR] = new Figure(mFigureDEDLISTDESTDIRFile);
         mFigures[mFigureDEDLISTDESTOA1] = new Figure(mFigureDEDLISTDESTOA1File);
         mFigures[mFigureDEDLISTDESTOA2] = new Figure(mFigureDEDLISTDESTOA2File);
         mFigures[mFigureDEDLISTBINGO] = new Figure(mFigureDEDLISTBINGOFile);
         mFigures[mFigureDEDLISTVIP] = new Figure(mFigureDEDLISTVIPFile);
         mFigures[mFigureDEDLISTINTG] = new Figure(mFigureDEDLISTINTGFile);
         mFigures[mFigureDEDLISTDLINK] = new Figure(mFigureDEDLISTDLINKFile);
         mFigures[mFigureDEDLISTMISC] = new Figure(mFigureDEDLISTMISCFile);
         mFigures[mFigureDEDLISTMISCCORR] = new Figure(mFigureDEDLISTMISCCORRFile);
         mFigures[mFigureDEDLISTMISCMAGV] = new Figure(mFigureDEDLISTMISCMAGVFile);
         mFigures[mFigureDEDLISTMISCOFP] = new Figure(mFigureDEDLISTMISCOFPFile);
         mFigures[mFigureDEDLISTMISCINSM] = new Figure(mFigureDEDLISTMISCINSMFile);
         mFigures[mFigureDEDLISTMISCLASR] = new Figure(mFigureDEDLISTMISCLASRFile);
         mFigures[mFigureDEDLISTMISCGPS] = new Figure(mFigureDEDLISTMISCGPSFile);
         mFigures[mFigureDEDLISTMISCDRNG] = new Figure(mFigureDEDLISTMISCDRNGFile);
         mFigures[mFigureDEDLISTMISCBULL] = new Figure(mFigureDEDLISTMISCBULLFile);
         mFigures[mFigureDEDLISTMISCWPT] = new Figure(mFigureDEDLISTMISCWPTFile);
         mFigures[mFigureDEDLISTNAV] = new Figure(mFigureDEDLISTNAVFile);
         mFigures[mFigureDEDLISTMAN] = new Figure(mFigureDEDLISTMANFile);
         mFigures[mFigureDEDLISTINS] = new Figure(mFigureDEDLISTINSFile);
         mFigures[mFigureDEDLISTEWS] = new Figure(mFigureDEDLISTEWSFile);
         mFigures[mFigureDEDLISTMODEAA] = new Figure(mFigureDEDLISTMODEAAFile);
         mFigures[mFigureDEDLISTMODEAG] = new Figure(mFigureDEDLISTMODEAGFile);
         mFigures[mFigureDEDLISTVRP] = new Figure(mFigureDEDLISTVRPFile);
         mFigures[mFigureThreatWingspans] = new Figure(mFigureThreatWingspansFile);

         //************************************************************************
         // DED Active Panel
         //************************************************************************

         APState apsDEDCNI = new APState(mFigures[mFigureDEDCNI]);

         apsDEDCNI.addTipArea(
            new TipArea(697,383,757,391,
               "Active Communications Channel",
               "The channel on which you're currently talking",
               mFigures[mFigureCOMMChannels],10,50));

         apsDEDCNI.addTipArea(
            new TipArea(767,383,806,391,
               "Current Steerpoint",
               "Indicates the selected steerpoint"));

         apsDEDCNI.addTipArea(
            new TipArea(697,402,757,410,
               "Secondary Communications Channel",
               "You can hear this channel, but not transmit",
               mFigures[mFigureCOMMChannels],10,50));

         apsDEDCNI.addTipArea(
            new TipArea(768,402,811,410,
               "HACK Clock",
               "Secondary clock use to keep track of time on station, etc."));

         APState apsDEDCNIWIND = new APState(mFigures[mFigureDEDCNIWIND]);

         apsDEDCNIWIND.copyTipAreas(apsDEDCNI);

         apsDEDCNIWIND.addTipArea(
            new TipArea(767,392,811,401,
               "Wind Direction and Speed",
               "In this example, 213 degs at 5 kts"));

         APState apsDEDILSTR = new APState(mFigures[mFigureDEDILSTR]);

         apsDEDILSTR.addTipArea(
            new TipArea(729,398,759,409,
               "TACAN channel entry",
               "Enter TACAN channel (1-126) and click ENTR or enter 0 and click ENTR to cycle TACAN bands (X/Y)"));

         apsDEDILSTR.addTipArea(
            new TipArea(696,411,757,418,
               "TACAN channel",
               "Displays currently selected TACAN channel (1-126)"));

         apsDEDILSTR.addTipArea(
            new TipArea(695,419,755,427,
               "TACAN Band",
               "band X or Y"));

         apsDEDILSTR.addTipArea(
            new TipArea(760,410,807,418,
               "ILS (Instrument Landing System) Frequency",
               "Displayed if TACAN is tuned into an airbase"));

         apsDEDILSTR.addTipArea(
            new TipArea(761,419,809,427,
               "CRS (Course)",
               "Displays course currently dialed into the HSI"));

         apsDEDILSTR.addTipArea(
             new TipArea(775,399,819,410,
               "Mode",
               "CMD STRG - Command Steering mode, HUD tadpole indicates a 45deg intercept to ILS approach path"));

         apsDEDILSTR.addTipArea(
            new TipArea(779,383,818,392,
               "ILS ON/OFF",
               "Indicates whether or not ILS is active"));

         APState apsDEDILSAATR = new APState(mFigures[mFigureDEDILSAATR]);

         apsDEDILSAATR.copyTipAreas(apsDEDILSTR);

         APState apsDEDALOW = new APState(mFigures[mFigureDEDALOW]);

         apsDEDALOW.addTipArea(
            new TipArea(711,402,817,409,
               "MSL Floor (Minimum Save Level)",
               "When descending below this level, Bitchin' Betty will call \"ALTITUDE-ALTITUDE\""));

         apsDEDALOW.addTipArea(
            new TipArea(695,410,816,419,
               "TF ADV (Terrain Following Advisory)",
               "* not implemented *"));

         apsDEDALOW.addTipArea(
            new TipArea(784,381,820,390,
               "Current Steerpoint",
               "Indicates the currently selected steerpoint"));

         APState apsDEDSTPTMAN = new APState(mFigures[mFigureDEDSTPTMAN]);

         apsDEDSTPTMAN.addTipArea(
            new TipArea(736,383,785,391,
               "Current Steerpoint",
               "The other info in this display applies to this steerpoint"));

         apsDEDSTPTMAN.addTipArea(
            new TipArea(794,383,814,391,
               "MAN/AUTO Steerpoint Selection",
               "MAN - manual, AUTO - steerpoint automatically advances when you're within 2 nm (unless FCC is in A-G mode)"));

         apsDEDSTPTMAN.addTipArea(
            new TipArea(721,393,809,409,
               "Steerpoint GPS Co-ordinates",
               "Displayed in degs, mins, and fractions of a minute"));

         apsDEDSTPTMAN.addTipArea(
            new TipArea(716,410,778,418,
               "Steerpoint GPS Elevation",
               "Feet above sea level of the steerpoint"));

         apsDEDSTPTMAN.addTipArea(
            new TipArea(721,419,803,428,
               "TOS (Time on Steerpoint)",
               "The time at which you are supposed to arrive at the steerpoint"));

         APState apsDEDSTPTAUTO = new APState(mFigures[mFigureDEDSTPTAUTO]);

         apsDEDSTPTAUTO.copyTipAreas(apsDEDSTPTMAN);

         APState apsDEDCRUSTOS = new APState(mFigures[mFigureDEDCRUSTOS]);

         apsDEDCRUSTOS.addTipArea(
            new TipArea(796,383,814,391,
               "Current Steerpoint",
               "Indicates the currently selected steerpoint"));

         apsDEDCRUSTOS.addTipArea(
            new TipArea(730,392,813,400,
               "System Time",
               "Current System (or if running, HACK) time"));

         apsDEDCRUSTOS.addTipArea(
            new TipArea(715,401,812,409,
               "TOS (Time on Steerpoint)",
               "The time at which you are supposed to arrive at the steerpoint"));

         apsDEDCRUSTOS.addTipArea(
            new TipArea(734,410,812,418,
               "ETA (Estimated Time of Arrival)",
               "An estimate of when you will arrive at the steerpoint"));

         apsDEDCRUSTOS.addTipArea(
            new TipArea(715,419,807,427,
               "Required GroundSpeed",
               "Reqd Groundspeed to arrive at the steerpoint on time; See LIST/INS page for groundspeed display"));

         APState apsDEDCRUSRNG = new APState(mFigures[mFigureDEDCRUSRNG]);

         apsDEDCRUSRNG.addTipArea(
            new TipArea(730,392,784,400,
               "Current Steerpoint",
               "Indicates the currently selected steerpoint"));

         apsDEDCRUSRNG.addTipArea(
            new TipArea(730,401,792,409,
               "Fuel Remaining at Steerpoint",
               "The estimated amount of remaining fuel you will have when you arrive at the steerpoint"));

         apsDEDCRUSRNG.addTipArea(
            new TipArea(730,419,812,427,
               "Wind Information",
               "Wind direction (degs) and speed (kts)"));

         APState apsDEDCRUSHOME = new APState(mFigures[mFigureDEDCRUSHOME]);

         apsDEDCRUSHOME.addTipArea(
            new TipArea(729,392,788,399,
               "HOME Steerpoint",
               "Indicates which steerpoint is your designated home steerpoint"));

         apsDEDCRUSHOME.addTipArea(
            new TipArea(729,401,792,409,
               "Fuel Remaining at Steerpoint",
               "The estimated amount of remaining fuel you will have when you arrive at the HOME steerpoint"));

         apsDEDCRUSHOME.addTipArea(
            new TipArea(715,411,799,418,
               "Optimum Cruise Altitude for HOME Steerpoint",
               ""));

         apsDEDCRUSHOME.addTipArea(
            new TipArea(728,420,812,428,
               "Wind Information",
               "Wind direction (degs) and speed (kts)"));

         APState apsDEDCRUSCDR = new APState(mFigures[mFigureDEDCRUSCDR]);

         apsDEDCRUSCDR.addTipArea(
            new TipArea(730,392,781,400,
               "Current Steerpoint",
               "Indicates the currently selected steerpoint"));

         apsDEDCRUSCDR.addTipArea(
            new TipArea(714,401,802,409,
               "Time until BINGO",
               "Amount of time remaining until you reach BINGO fuel level"));

         apsDEDCRUSCDR.addTipArea(
            new TipArea(710,410,783,419,
               "Optimum Cruise Speed",
               "As a fraction of the speed of sound (mach)"));

         apsDEDCRUSCDR.addTipArea(
            new TipArea(729,419,814,428,
               "Wind Information",
               "Wind direction (degs) and speed (kts)"));

         APState apsDEDTIME = new APState(mFigures[mFigureDEDTIME]);

         apsDEDTIME.addTipArea(
            new TipArea(714,400,811,410,
               "Current System Time",
               ""));

         apsDEDTIME.addTipArea(
            new TipArea(725,410,804,418,
               "HACK Time (Stopwatch)",
               "ICP NEXT to start/freeze/update, and ICP PREV to clear"));

         apsDEDTIME.addTipArea(
            new TipArea(700,420,802,428,
               "DELTA TOS",
               ""));

         APState apsDEDMARK = new APState(mFigures[mFigureDEDMARK]);

         apsDEDMARK.addTipArea(
            new TipArea(722,396,795,423,
               "MARK Point Information",
               "ICP ENTR to set a mark point (your pos or if A-G lock, the target pos)"));

         APState apsDEDFIX = new APState(mFigures[mFigureDEDFIX]);

         apsDEDFIX.addTipArea(
            new TipArea(734,392,779,401,
               "Current Steerpoint",
               "Indicates the currently selected steerpoint"));

         apsDEDFIX.addTipArea(
            new TipArea(724,401,805,428,
               "INS Position Sensors",
               "Details on INS Alignment Sensors"));

         APState apsDEDACAL = new APState(mFigures[mFigureDEDACAL]);

         apsDEDACAL.addTipArea(
            new TipArea(793,383,820,391,
               "Current Steerpoint",
               "Indicates the currently selected steerpoint"));

         apsDEDACAL.addTipArea(
            new TipArea(698,393,808,428,
               "Update System Altitude and/or INS Position",
               ""));

         APState apsDEDCOMM1 = new APState(mFigures[mFigureDEDCOMM1]);
         APState apsDEDCOMM2 = new APState(mFigures[mFigureDEDCOMM2]);
         APState apsDEDIFF = new APState(mFigures[mFigureDEDIFF]);

         apsDEDIFF.addTipArea(
            new TipArea(700,381,822,429,
               "IFF (Identify Friend or Foe)",
               "Displays info about IFF settings; not implemented in the sim"));

         APState apsDEDLIST = new APState(mFigures[mFigureDEDLIST]);

         apsDEDLIST.addTipArea(
            new TipArea(789,382,820,390,
               "Current Steerpoint",
               "Indicates the currently selected steerpoint"));

         apsDEDLIST.addTipArea(
            new TipArea(695,391,820,420,
               "LIST Page",
               "Use the indicated ICP buttons to access the subpages"));

         APState apsDEDLISTDESTDIR = new APState(mFigures[mFigureDEDLISTDESTDIR]);

         apsDEDLISTDESTDIR.addTipArea(
            new TipArea(795,383,820,392,
               "Current Steerpoint",
               "Indicates the currently selected steerpoint"));

         apsDEDLISTDESTDIR.addTipArea(
            new TipArea(715,392,816,409,
               "Steerpoint GPS Co-ordinates",
               "Allows you to edit the co-ordinates for a steerpoint"));

         apsDEDLISTDESTDIR.addTipArea(
            new TipArea(711,410,795,418,
               "ELEV",
               "Steerpoint Altitude"));

         apsDEDLISTDESTDIR.addTipArea(
            new TipArea(716,420,810,428,
               "TOS (Time on Steerpoint)",
               "The time at which you are supposed to arrive at the steerpoint"));

         APState apsDEDLISTDESTOA1 = new APState(mFigures[mFigureDEDLISTDESTOA1]);

         apsDEDLISTDESTOA1.addTipArea(
            new TipArea(795,383,820,392,
               "Current Steerpoint",
               "Indicates the currently selected steerpoint"));

         apsDEDLISTDESTOA1.addTipArea(
            new TipArea(715,400,790,409,
               "OA1 (Offset Aimpoint 1) - Range",
               "Range of OA1 from the selected steerpoint"));

         apsDEDLISTDESTOA1.addTipArea(
            new TipArea(716,410,772,418,
               "OA1 (Offset Aimpoint 1) - Bearing",
               "Bearing of OA1 from the selected steerpoint"));

         apsDEDLISTDESTOA1.addTipArea(
            new TipArea(710,419,786,428,
               "OA1 (Offset Aimpoint 1) - Altitude",
               ""));

         APState apsDEDLISTDESTOA2 = new APState(mFigures[mFigureDEDLISTDESTOA2]);

         apsDEDLISTDESTOA2.addTipArea(
            new TipArea(795,383,820,392,
               "Current Steerpoint",
               "Indicates the currently selected steerpoint"));

         apsDEDLISTDESTOA2.addTipArea(
            new TipArea(715,400,790,409,
               "OA2 (Offset Aimpoint 2) - Range",
               "Range of OA2 from the selected steerpoint"));

         apsDEDLISTDESTOA2.addTipArea(
            new TipArea(716,410,772,418,
               "OA2 (Offset Aimpoint 2) - Bearing",
               "Bearing of OA2 from the selected steerpoint"));

         apsDEDLISTDESTOA2.addTipArea(
            new TipArea(710,419,786,428,
               "OA2 (Offset Aimpoint 2) - Altitude",
               ""));

         APState apsDEDLISTBINGO = new APState(mFigures[mFigureDEDLISTBINGO]);

         apsDEDLISTBINGO.addTipArea(
            new TipArea(795,383,820,392,
               "Current Steerpoint",
               "Indicates the currently selected steerpoint"));

         apsDEDLISTBINGO.addTipArea(
            new TipArea(716,400,797,411,
               "Bingo Fuel Level",
               "Sets the Fuel Level which is considered BINGO"));

         apsDEDLISTBINGO.addTipArea(
            new TipArea(706,411,784,419,
               "Remaining Fuel",
               "Total Remaining Fuel"));

         APState apsDEDLISTVIP = new APState(mFigures[mFigureDEDLISTVIP]);

         apsDEDLISTVIP.addTipArea(
            new TipArea(734,392,779,400,
               "Current Steerpoint",
               "Indicates the currently selected steerpoint"));

         apsDEDLISTVIP.addTipArea(
            new TipArea(731,402,802,428,
               "Visual Initial Point (VIP) setting",
               "Allows setting up VIP bearing/range/alt for the steerpoint"));

         APState apsDEDLISTINTG = new APState(mFigures[mFigureDEDLISTINTG]);

         apsDEDLISTINTG.addTipArea(
            new TipArea(696,397,822,428,
               "Interrogation Page (INTG)",
               "Check or Set AIFF modes and interrogation code"));

         APState apsDEDLISTDLINK = new APState(mFigures[mFigureDEDLISTDLINK]);

         apsDEDLISTDLINK.addTipArea(
            new TipArea(713,384,789,404,
               "Data Link Page (DLNK)",
               "a secure radio channel for receiving targeting information; Look for floating steerpoint (small circle) on your HUD"));

         APState apsDEDLISTMISC = new APState(mFigures[mFigureDEDLISTMISC]);

         apsDEDLISTMISC.addTipArea(
            new TipArea(689,384,802,424,
               "MISC",
               "Access subpages using indicated ICP buttons"));

         APState apsDEDLISTMISCCORR = new APState(mFigures[mFigureDEDLISTMISCCORR]);

         apsDEDLISTMISCCORR.addTipArea(
            new TipArea(699,383,819,422,
               "Correction Page (CORR)",
               "Check/set correction coefficient for HUD, CTVS, CAMERA, and left/right hardpoints (N/I)"));

         APState apsDEDLISTMISCMAGV = new APState(mFigures[mFigureDEDLISTMISCMAGV]);

         apsDEDLISTMISCMAGV.addTipArea(
            new TipArea(699,383,819,422,
               "Magnetic Variation Page (MAGV)",
               "Actual magnetic variation at the aircraft's location; used to correct INS errors (N/I)"));

         APState apsDEDLISTMISCOFP = new APState(mFigures[mFigureDEDLISTMISCOFP]);

         apsDEDLISTMISCOFP.addTipArea(
            new TipArea(704,381,819,429,
               "Operational Flight Program Page (OFP)",
               "Shows program numbers for various systems (N/I)"));

         APState apsDEDLISTMISCINSM = new APState(mFigures[mFigureDEDLISTMISCINSM]);

         apsDEDLISTMISCINSM.addTipArea(
            new TipArea(731,383,775,414,
               "Inertial Navigation System Memory Page (INSM)",
               "INS parameters (drift errors, maintenance data, mfg codes) (N/I)"));

         APState apsDEDLISTMISCLASR = new APState(mFigures[mFigureDEDLISTMISCLASR]);

         apsDEDLISTMISCLASR.addTipArea(
            new TipArea(704,381,819,430,
               "Laser Page (LASR)",
               "Set targeting pod laser pulse code (N/I) and laser timing"));

         APState apsDEDLISTMISCGPS = new APState(mFigures[mFigureDEDLISTMISCGPS]);

         apsDEDLISTMISCGPS.addTipArea(
            new TipArea(694,382,818,430,
               "Global Positioning System (GPS) Page",
               "Displays info about the GPS system (N/I)"));

         APState apsDEDLISTMISCDRNG = new APState(mFigures[mFigureDEDLISTMISCDRNG]);

         apsDEDLISTMISCDRNG.addTipArea(
            new TipArea(707,392,819,422,
               "DRNG Page",
               "Set manual correction to a consistent A-G miss distance (N/I)"));

         APState apsDEDLISTMISCBULL = new APState(mFigures[mFigureDEDLISTMISCBULL]);

         apsDEDLISTMISCBULL.addTipArea(
            new TipArea(724,378,789,403,
               "BULLSEYE mode",
               "Toggle with ICP 0 and ENTR; If on, bullseye pos of the aircraft is displayed in the HSD"));

         APState apsDEDLISTMISCWPT = new APState(mFigures[mFigureDEDLISTMISCWPT]);

         apsDEDLISTMISCWPT.addTipArea(
            new TipArea(702,381,812,427,
               "WPT Page",
               "Info/Settings for Harpoon missile operation (N/I)"));

         APState apsDEDLISTNAV = new APState(mFigures[mFigureDEDLISTNAV]);

         apsDEDLISTNAV.addTipArea(
            new TipArea(795,384,819,392,
               "Current Steerpoint",
               "Indicates the currently selected steerpoint"));

         apsDEDLISTNAV.addTipArea(
            new TipArea(715,392,816,429,
               "Navigation Page (NAV)",
               "Displays and controls FCC NAV Filter operation & GPS functions; Not implemented"));

         APState apsDEDLISTMAN = new APState(mFigures[mFigureDEDLISTMAN]);

         apsDEDLISTMAN.addTipArea(
            new TipArea(795,384,819,392,
               "Current Steerpoint",
               "Indicates the currently selected steerpoint"));

         apsDEDLISTMAN.addTipArea(
            new TipArea(720,392,791,402,
               "Manual Gun Funneling Wingspan Adjustment",
               "Adjust gun funnel width to match the wingspan of known threats",
               mFigures[mFigureThreatWingspans],10,50));

         APState apsDEDLISTINS = new APState(mFigures[mFigureDEDLISTINS]);

         apsDEDLISTINS.addTipArea(
            new TipArea(795,384,819,392,
               "Current Steerpoint",
               "Indicates the currently selected steerpoint"));

         apsDEDLISTINS.addTipArea(
            new TipArea(715,382,773,391,
               "INS (Inertial Nagivation System) Alignment Status",
               ""));

         apsDEDLISTINS.addTipArea(
            new TipArea(715,393,813,410,
               "Current Position",
               ""));

         apsDEDLISTINS.addTipArea(
            new TipArea(710,411,803,420,
               "Altitude",
               ""));

         apsDEDLISTINS.addTipArea(
            new TipArea(706,420,766,428,
               "True Heading",
               ""));

         apsDEDLISTINS.addTipArea(
            new TipArea(775,420,820,427,
               "Current Groundspeed",
               ""));

         APState apsDEDLISTEWS = new APState(mFigures[mFigureDEDLISTEWS]);

         apsDEDLISTEWS.addTipArea(
            new TipArea(795,384,819,392,
               "Current Steerpoint",
               "Indicates the currently selected steerpoint"));

         apsDEDLISTEWS.addTipArea(
            new TipArea(755,392,813,400,
               "REQJAM ON/OFF",
               "If ON, Jammer is automatically turned on when RWR detects a radar spike"));

         apsDEDLISTEWS.addTipArea(
            new TipArea(755,419,813,427,
               "BINGO ON/OFF",
               "If ON, warning will be enabled for chaff/flares (set CH/FL warning levels)"));

         apsDEDLISTEWS.addTipArea(
            new TipArea(701,392,749,409,
               "Chaff/Flare warning levels",
               "If BINGO is ON, these levels determine when a warning is provided"));

         APState apsDEDLISTMODEAA = new APState(mFigures[mFigureDEDLISTMODEAA]);

         apsDEDLISTMODEAA.addTipArea(
            new TipArea(800,382,820,391,
               "Current Steerpoint",
               "Indicates the currently selected steerpoint"));

         apsDEDLISTMODEAA.addTipArea(
            new TipArea(740,381,795,392,
               "Master Mode Override (AA)",
               "Allows invoking the AA override in case the ICP buttons become damaged"));

         APState apsDEDLISTMODEAG = new APState(mFigures[mFigureDEDLISTMODEAG]);

         apsDEDLISTMODEAG.addTipArea(
            new TipArea(800,382,820,391,
               "Current Steerpoint",
               "Indicates the currently selected steerpoint"));

         apsDEDLISTMODEAG.addTipArea(
            new TipArea(740,381,795,392,
               "Master Mode Override (AA)",
               "Allows invoking the AA override in case the ICP buttons become damaged"));

         APState apsDEDLISTVRP = new APState(mFigures[mFigureDEDLISTVRP]);

         apsDEDLISTVRP.addTipArea(
            new TipArea(729,391,779,400,
               "Visual Reference Point (VRP) Steerpoint",
               "Allows setting VRP for the specified steerpoint"));

         apsDEDLISTVRP.addTipArea(
            new TipArea(730,402,803,427,
               "VRP location",
               "Bearing/Range/Alt for Visual Reference Point (VRP) for the selected steerpoint"));

         apsDEDCNIWIND.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDILSTR.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDILSAATR.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDALOW.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDSTPTMAN.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDSTPTAUTO.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDCRUSTOS.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDCRUSRNG.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDCRUSHOME.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDCRUSCDR.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDTIME.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDMARK.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDFIX.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDACAL.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDCOMM1.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDCOMM2.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDIFF.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLIST.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTDESTDIR.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTDESTOA1.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTDESTOA2.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTBINGO.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTVIP.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTINTG.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTDLINK.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTMISC.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTMISCCORR.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTMISCMAGV.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTMISCOFP.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTMISCINSM.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTMISCLASR.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTMISCGPS.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTMISCDRNG.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTMISCBULL.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTMISCWPT.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTNAV.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTMAN.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTINS.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTEWS.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTMODEAA.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTMODEAG.addTransition(PitEvent.ICP_RTN,apsDEDCNI);
         apsDEDLISTVRP.addTransition(PitEvent.ICP_RTN,apsDEDCNI);

         apsDEDCNI.addTransition(PitEvent.ICP_SEQ,apsDEDCNIWIND);
         apsDEDCNIWIND.addTransition(PitEvent.ICP_SEQ,apsDEDCNI);

         apsDEDCNI.addTransition(PitEvent.ICP_ILS,apsDEDILSTR);
         apsDEDCNI.addTransition(PitEvent.ICP_ALOW,apsDEDALOW);
         apsDEDCNI.addTransition(PitEvent.ICP_3,apsDEDLISTDLINK);
         apsDEDCNI.addTransition(PitEvent.ICP_STPT,apsDEDSTPTMAN);
         apsDEDCNI.addTransition(PitEvent.ICP_CRUS,apsDEDCRUSTOS);
         apsDEDCNI.addTransition(PitEvent.ICP_TIME,apsDEDTIME);
         apsDEDCNI.addTransition(PitEvent.ICP_MARK,apsDEDMARK);
         apsDEDCNI.addTransition(PitEvent.ICP_FIX,apsDEDFIX);
         apsDEDCNI.addTransition(PitEvent.ICP_ACAL,apsDEDACAL);
         apsDEDCNI.addTransition(PitEvent.ICP_COMM1,apsDEDCOMM1);
         apsDEDCNI.addTransition(PitEvent.ICP_COMM2,apsDEDCOMM2);
         apsDEDCNI.addTransition(PitEvent.ICP_IFF,apsDEDIFF);
         apsDEDCNI.addTransition(PitEvent.ICP_LIST,apsDEDLIST);

         apsDEDCNIWIND.addTransition(PitEvent.ICP_ILS,apsDEDILSTR);
         apsDEDCNIWIND.addTransition(PitEvent.ICP_ALOW,apsDEDALOW);
         apsDEDCNIWIND.addTransition(PitEvent.ICP_3,apsDEDLISTDLINK);
         apsDEDCNIWIND.addTransition(PitEvent.ICP_STPT,apsDEDSTPTMAN);
         apsDEDCNIWIND.addTransition(PitEvent.ICP_CRUS,apsDEDCRUSTOS);
         apsDEDCNIWIND.addTransition(PitEvent.ICP_TIME,apsDEDTIME);
         apsDEDCNIWIND.addTransition(PitEvent.ICP_MARK,apsDEDMARK);
         apsDEDCNIWIND.addTransition(PitEvent.ICP_FIX,apsDEDFIX);
         apsDEDCNIWIND.addTransition(PitEvent.ICP_ACAL,apsDEDACAL);
         apsDEDCNIWIND.addTransition(PitEvent.ICP_COMM1,apsDEDCOMM1);
         apsDEDCNIWIND.addTransition(PitEvent.ICP_COMM2,apsDEDCOMM2);
         apsDEDCNIWIND.addTransition(PitEvent.ICP_IFF,apsDEDIFF);
         apsDEDCNIWIND.addTransition(PitEvent.ICP_LIST,apsDEDLIST);

         apsDEDILSTR.addTransition(PitEvent.ICP_SEQ,apsDEDILSAATR);
         apsDEDILSAATR.addTransition(PitEvent.ICP_SEQ,apsDEDILSTR);

         apsDEDSTPTMAN.addTransition(PitEvent.ICP_SEQ,apsDEDSTPTAUTO);
         apsDEDSTPTAUTO.addTransition(PitEvent.ICP_SEQ,apsDEDSTPTMAN);

         apsDEDCRUSTOS.addTransition(PitEvent.ICP_SEQ,apsDEDCRUSRNG);
         apsDEDCRUSRNG.addTransition(PitEvent.ICP_SEQ,apsDEDCRUSHOME);
         apsDEDCRUSHOME.addTransition(PitEvent.ICP_SEQ,apsDEDCRUSCDR);
         apsDEDCRUSCDR.addTransition(PitEvent.ICP_SEQ,apsDEDCRUSTOS);

         apsDEDLIST.addTransition(PitEvent.ICP_ILS,apsDEDLISTDESTDIR);
         apsDEDLISTDESTDIR.addTransition(PitEvent.ICP_SEQ,apsDEDLISTDESTOA1);
         apsDEDLISTDESTOA1.addTransition(PitEvent.ICP_SEQ,apsDEDLISTDESTOA2);
         apsDEDLISTDESTOA2.addTransition(PitEvent.ICP_SEQ,apsDEDLISTDESTDIR);
         apsDEDLIST.addTransition(PitEvent.ICP_ALOW,apsDEDLISTBINGO);
         apsDEDLIST.addTransition(PitEvent.ICP_3,apsDEDLISTVIP);
         apsDEDLIST.addTransition(PitEvent.ICP_RCL,apsDEDLISTINTG);
         apsDEDLIST.addTransition(PitEvent.ICP_STPT,apsDEDLISTNAV);
         apsDEDLIST.addTransition(PitEvent.ICP_CRUS,apsDEDLISTMAN);
         apsDEDLIST.addTransition(PitEvent.ICP_TIME,apsDEDLISTINS);
         apsDEDLIST.addTransition(PitEvent.ICP_ENTR,apsDEDLISTDLINK);
         apsDEDLIST.addTransition(PitEvent.ICP_MARK,apsDEDLISTEWS);
         apsDEDLIST.addTransition(PitEvent.ICP_FIX,apsDEDLISTMODEAA);
         apsDEDLIST.addTransition(PitEvent.ICP_ACAL,apsDEDLISTVRP);
         apsDEDLIST.addTransition(PitEvent.ICP_MSEL,apsDEDLISTMISC);

         apsDEDLISTMISC.addTransition(PitEvent.ICP_ILS,apsDEDLISTMISCCORR);
         apsDEDLISTMISC.addTransition(PitEvent.ICP_ALOW,apsDEDLISTMISCMAGV);
         apsDEDLISTMISC.addTransition(PitEvent.ICP_3,apsDEDLISTMISCOFP);
         apsDEDLISTMISC.addTransition(PitEvent.ICP_STPT,apsDEDLISTMISCINSM);
         apsDEDLISTMISC.addTransition(PitEvent.ICP_CRUS,apsDEDLISTMISCLASR);
         apsDEDLISTMISC.addTransition(PitEvent.ICP_TIME,apsDEDLISTMISCGPS);
         apsDEDLISTMISC.addTransition(PitEvent.ICP_MARK,apsDEDLISTMISCDRNG);
         apsDEDLISTMISC.addTransition(PitEvent.ICP_FIX,apsDEDLISTMISCBULL);
         apsDEDLISTMISC.addTransition(PitEvent.ICP_ACAL,apsDEDLISTMISCWPT);

         ActivePanel apDED = new ActivePanel(678,372);
         apDED.setCurrentState(apsDEDCNI);
         apDED.addOverride(PitEvent.ICP_COMM1,apsDEDCOMM1);
         apDED.addOverride(PitEvent.ICP_COMM2,apsDEDCOMM2);
         apDED.addOverride(PitEvent.ICP_IFF,apsDEDIFF);
         apDED.addOverride(PitEvent.ICP_LIST,apsDEDLIST);
         mPositions[0].addActivePanel(apDED);

         //************************************************************************
         // tip areas
         //************************************************************************

         // the generic tip area displayed when no other tips are moused over
         mGenericTipArea = new TipArea(-1,-1,-1,-1,
            "Pit Trainer",
            "Use arrow keys or numpad to change view; Mouse-over controls to see more information");

         //************************************************************************
         // tip areas (POSITION 0)
         //************************************************************************

         mPositions[0].addTipArea(
            new TipArea(299,244,327,320,
               "AOA Indexer",
               "\\/ - too high, O - correct, /\\ - too low"));

         mPositions[0].addTipArea(
            new TipArea(687,246,715,323,
               "A/R Panel",
               "RDY - cleared for contact by tanker, AR/NWS - active refuel or nosewheel steering, DISC - tanker disconnect"));

         poly = new Polygon();
         poly.addPoint(112,442);
         poly.addPoint(147,398);
         poly.addPoint(174,418);
         poly.addPoint(135,465);
         mPositions[0].addTipArea(
            new TipArea(poly,
               "Master Caution Light",
               "Indicates another caution light has been lit, click to clear it"));

         poly = new Polygon();
         poly.addPoint(69,511);
         poly.addPoint(85,492);
         poly.addPoint(105,510);
         poly.addPoint(89,530);
         mPositions[0].addTipArea(
            new TipArea(poly,
               "TF-FAIL (Left Eyebrow Warning Light)",
               "Indicates a failure in the terrain following system"));

         mPositions[0].addTipArea(
            new TipArea(242,409,351,536,
               "RWR (Radar Warning Receiver)",
               "Shows radar tracking symbols by bearing and signal strength",
               mFigures[mFigureRWRSymbology],10,50));

         mPositions[0].addTipArea(
            new TipArea(202,423,230,448,
               "HANDOFF",
               "Override which threat is selected handing off to the next highest pri.  Lit when highest pri not selected."));

         mPositions[0].addTipArea(
            new TipArea(202,454,230,481,
               "Priority Mode",
               "When lit, only the 5 highest pri threats shown, otherwise up to 16 threats shown."));

         mPositions[0].addTipArea(
            new TipArea(202,489,230,516,
               "TGT SEP",
               "Press to unstack threat symbols which overlap each other, decluttering the display."));

         mPositions[0].addTipArea(
            new TipArea(169,454,196,481,
               "LAUNCH",
               "Flashes when a radar guided missile launch has been detected (heat-seekers (IR) are not detected."));

         mPositions[0].addTipArea(
            new TipArea(136,489,162,516,
               "NAVAL",
               "Press to increase pri of Naval threats which are normally lower pri than air or ground threats."));

         mPositions[0].addTipArea(
            new TipArea(358,392,379,460,
               "HUD Brightness Wheel",
               "Left-click to increase brightness, Right-click to decrease brightness"));

         mPositions[0].addTipArea(
            new TipArea(669,366,846,436,
               "Data Entry Display (DED)",
               "Information display controlled by the buttons on the ICP"));

         mPositions[0].addTipArea(
            new TipArea(405,530,426,570,
               "Increment/Decrement",
               "Used to cycle through values on the DED.  The value which is cycled is the one with the small arrows."));

         TipArea taRTN = new TipArea(443,537,471,565,
               "RETURN Data Command Switch (DCS)",
               "Returns the DED back to the default (CNI) page from any other page.");
         taRTN.setEvent(PitEvent.ICP_RTN);
         mPositions[0].addTipArea(taRTN);

         mPositions[0].addTipArea(
            new TipArea(576,390,16,
               "Air-To-Air Mode",
               "Changes HUD to Missile mode, left MFD to RWS, and right MFD to missile stores page."));

         mPositions[0].addTipArea(
            new TipArea(620,390,16,
               "Air-To-Ground Mode",
               "Changes HUD to CCIP mode, left MFD to GMT, and right MFD to A-G CCIP weapon page for bombing."));

         TipArea taCOMM1 = new TipArea(399,390,16,
               "COMM1 Override (click again to return)",
               "Selects channel 1 for transmit and receive.  Selected channel info will show as first line in DED CNI page.",
               mFigures[mFigureCOMMChannels],10,50);
         taCOMM1.setEvent(PitEvent.ICP_COMM1);
         mPositions[0].addTipArea(taCOMM1);

         TipArea taCOMM2 = new TipArea(443,390,16,
               "COMM2 Override (click again to return)",
               "Selects channel 2 for transmit and receive.  Selected channel info will show as first line in DED CNI page.",
               mFigures[mFigureCOMMChannels],10,50);
         taCOMM2.setEvent(PitEvent.ICP_COMM2);
         mPositions[0].addTipArea(taCOMM2);

         TipArea taIFF = new TipArea(488,390,15,
               "IFF (Identify Friend or Foe)",
               "Brings up the IFF page on the DED");
         taIFF.setEvent(PitEvent.ICP_IFF);
         mPositions[0].addTipArea(taIFF);

         TipArea taLIST = new TipArea(532,390,16,
               "List (Selects List DED Page)",
               "Steerpoint co-ord mod, Set Bingo fuel level, Visual Initial Waypoint, Gun Ballistics Settings, INS, EWS Progs, etc.");
         taLIST.setEvent(PitEvent.ICP_LIST);
         mPositions[0].addTipArea(taLIST);

         mPositions[0].addTipArea(
            new TipArea(468,525,483,538,
               "UP Data Command Switch (DCS)",
               "Cycle Forward through editable options on the DED"));

         mPositions[0].addTipArea(
            new TipArea(468,559,483,579,
               "DOWN Data Command Switch (DCS)",
               "Cycle Backwards through editable options on the DED"));

         TipArea taSEQ = new TipArea(486,537,512,565,
               "SEQ Data Command Switch (DCS)",
               "Cycle through subpages and options on the DED");
         taSEQ.setEvent(PitEvent.ICP_SEQ);
         mPositions[0].addTipArea(taSEQ);

         TipArea taILS = new TipArea(398,416,426,442,
               "T-ILS (Selects TACAN/ILS DED Page)",
               "change TACAN channel and domain, shows HSI course, and toggle ILS");
         taILS.setEvent(PitEvent.ICP_ILS);
         mPositions[0].addTipArea(taILS);

         TipArea taALOW = new TipArea(440,416,468,442,
               "ALOW (Selects Altitude Low DED Page)",
               "Used to set your low altitude warning limit which will also display as a sideways \"T\" on the alt tape.");
         taALOW.setEvent(PitEvent.ICP_ALOW);
         mPositions[0].addTipArea(taALOW);

         TipArea ta3 = new TipArea(484,417,510,442,
               "3",
               "");
         ta3.setEvent(PitEvent.ICP_3);
         mPositions[0].addTipArea(ta3);

         TipArea taRCL = new TipArea(539,417,565,442,
               "RCL",
               "");
         taRCL.setEvent(PitEvent.ICP_RCL);
         mPositions[0].addTipArea(taRCL);

         TipArea taENTR = new TipArea(539,459,566,484,
               "ENTR",
               "");
         taENTR.setEvent(PitEvent.ICP_ENTR);
         mPositions[0].addTipArea(taENTR);

         TipArea taSTPT = new TipArea(398,455,426,482,
               "STPT (Selects Steerpoint Info DED Page)",
               "Provides GPS Co-ordinates of the selected steerpoint and toggle between auto and manual steerpoint switching.");
         taSTPT.setEvent(PitEvent.ICP_STPT);
         mPositions[0].addTipArea(taSTPT);

         TipArea taCRUS = new TipArea(440,455,468,482,
               "CRUS (Selects Cruise Management DED Page)",
               "Remaining Fuel estimation, Optimum cruise speed, wind info, and Time on Steerpoint (TOS) info.");
         taCRUS.setEvent(PitEvent.ICP_CRUS);
         mPositions[0].addTipArea(taCRUS);

         TipArea taTIME = new TipArea(483,455,511,482,
               "TIME (Selects Time DED Page)",
               "Shows current time, provides stopwatch");
         taTIME.setEvent(PitEvent.ICP_TIME);
         mPositions[0].addTipArea(taTIME);

         TipArea taMARK = new TipArea(398,495,426,522,
               "MARK (Selects Mark DED Page)",
               "Shows info on Mark Points");
         taMARK.setEvent(PitEvent.ICP_MARK);
         mPositions[0].addTipArea(taMARK);

         TipArea taFIX = new TipArea(440,495,468,522,
               "FIX (Selects Fix DED Page)",
               "Permits selection of which sensors update INS position");
         taFIX.setEvent(PitEvent.ICP_FIX);
         mPositions[0].addTipArea(taFIX);

         TipArea taACAL = new TipArea(483,495,511,522,
               "A-CAL (Selects A-CAL DED Page)",
               "Used to update system altitude and/or INS position");
         taACAL.setEvent(PitEvent.ICP_ACAL);
         mPositions[0].addTipArea(taACAL);

         TipArea taMSEL = new TipArea(525,495,553,522,
               "Mode Select",
               "");
         taMSEL.setEvent(PitEvent.ICP_MSEL);
         mPositions[0].addTipArea(taMSEL);

         mPositions[0].addTipArea(
            new TipArea(519,527,558,540,
               "Drift C/O",
               "Ensures HUD pitch ladder is always centered (otherwise it can drift due to wind effects)"));

         mPositions[0].addTipArea(
            new TipArea(520,556,558,574,
               "MAN RESET",
               "Cancels any pending warning messages"));

         mPositions[0].addTipArea(
            new TipArea(676,457,757,535,
               "Attitude Direction Indicator (ADI)",
               "Displays the pitch and roll of the aircraft."));

         mPositions[0].addTipArea(
            new TipArea(787,483,847,530,
               "Fuel Flow",
               "Shows total fuel flow in pounds per hour.  Compare this against remaining pounds of fuel to guestimate flight time remaining."));

         poly = new Polygon();
         poly.addPoint(882,455);
         poly.addPoint(900,439);
         poly.addPoint(915,460);
         poly.addPoint(898,475);
         mPositions[0].addTipArea(
            new TipArea(poly,
               "ENG FIRE (Right Eyebrow Warning Light)",
               "If lit, there is a fire in your engine.  Your only recourse is to eject before your plane explodes (hold Ctrl-E)."));

         poly = new Polygon();
         poly.addPoint(900,480);
         poly.addPoint(921,462);
         poly.addPoint(936,481);
         poly.addPoint(916,500);
         mPositions[0].addTipArea(
            new TipArea(poly,
               "HYD/OIL PRESS (Right Eyebrow Warning Light)",
               "Indicates low pressure in hydraulic systems or low oil engine pressure.  Engine will eventually freeze up."));

         poly = new Polygon();
         poly.addPoint(920,504);
         poly.addPoint(938,488);
         poly.addPoint(954,508);
         poly.addPoint(935,524);
         mPositions[0].addTipArea(
            new TipArea(poly,
               "FLCS DBU ON (Right Eyebrow Warning Light)",
               "Indicates a problem with the dual flight control system (2 or more FLCS computers offline).  Significantly reduced flight control reliability.  RTB ASAP."));

         poly = new Polygon();
         poly.addPoint(940,529);
         poly.addPoint(957,513);
         poly.addPoint(972,532);
         poly.addPoint(954,547);
         mPositions[0].addTipArea(
            new TipArea(poly,
               "T/L CFG (Right Eyebrow Warning Light)",
               "Indicates your configuration is incorrect for takeoff/landing (gear not down)."));

         poly = new Polygon();
         poly.addPoint(958,554);
         poly.addPoint(975,539);
         poly.addPoint(991,559);
         poly.addPoint(973,574);
         mPositions[0].addTipArea(
            new TipArea(poly,
               "CANOPY (Right Eyebrow Warning Light)",
               "Indicates a problem with cockpit pressurization."));

         mPositions[0].addTipArea(
            new TipArea(92,557,145,604,
               "R/F (Allows switching off detectable emissions)",
               "Norm - Normal mode, Quiet - Radar in standby/emissions reduced, Silent - ALL emissions silenced (RADAR, CARA (RALT), TFR)"));

         mPositions[0].addTipArea(
            new TipArea(109,609,141,642,
               "ECM",
               "Lit if Jammer is active"));

         mPositions[0].addTipArea(
            new TipArea(63,617,101,667,
               "Laser Arm (Turns on FLIR pod's targeting laser)",
               "Laser Guided Bombs require this to be on to illuminate the target.  If the laser stops or radar lock is lost, wpn follows ballistic trajectory."));

         mPositions[0].addTipArea(
            new TipArea(116,674,9,
               "Alt Rel (Alternate Weapons Release)",
               ""));

         mPositions[0].addTipArea(
            new TipArea(85,705,136,749,
               "Master Arm (Controls Access to all Weapon Systems)",
               "Off - Inactive, Sim - Active but launch is prohibited, Arm - Fully Active (armed and dangerous)"));

         mPositions[0].addTipArea(
            new TipArea(186,571,357,737,
               "Left Multi-function Display (MFD)",
               "Cycle through displays using '[' or use the OSBs to switch to the desired display"));

         mPositions[0].addTipArea(
            new TipArea(664,572,834,737,
               "Right Multi-function Display (MFD)",
               "Cycle through displays using ']' or use the OSBs to switch to the desired display"));

         mPositions[0].addTipArea(
            new TipArea(907,588,22,
               "Oil Pressure (Engine Oil Pressure)",
               "Ranging from 0 to 100 pounds per square inch (psi).  If this drops below 15 psi, you have a serious oil pressure leak."));

         mPositions[0].addTipArea(
            new TipArea(915,650,32,
               "Nozzle Pos (Position of the Engine Nozzle)",
               "Mostly open at idle, closed at Mil power (100% RPM), and fully open at Afterburner"));

         mPositions[0].addTipArea(
            new TipArea(920,722,38,
               "RPM (Engine Revolutions Per Minute)",
               "Expressed as a percentage from 0% to 100%.  75% - idle, 100% - full military power (just before Afterburner kicks in)"));

         mPositions[0].addTipArea(
            new TipArea(445,689,53,
               "Airspeed/Mach Disk",
               "Current Airspeed (80 to 800 knots) on outside dial, Mach Factor (speed relative to the speed of sound)"));

         mPositions[0].addTipArea(
            new TipArea(570,685,53,
               "Barometric Altimeter",
               "Displays altitude above sea level (which may not be the same as altitude above ground level)"));

         // left MFD OSB buttons

         mPositions[0].addTipArea(
            new TipArea(211,549,225,564,
               "OSB 1 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[0].addTipArea(
            new TipArea(238,549,252,564,
               "OSB 2 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[0].addTipArea(
            new TipArea(264,549,279,564,
               "OSB 3 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[0].addTipArea(
            new TipArea(291,549,305,564,
               "OSB 4 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[0].addTipArea(
            new TipArea(317,549,331,564,
               "OSB 5 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[0].addTipArea(
            new TipArea(364,600,377,617,
               "OSB 6 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[0].addTipArea(
            new TipArea(364,625,377,641,
               "OSB 7 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[0].addTipArea(
            new TipArea(364,648,377,665,
               "OSB 8 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[0].addTipArea(
            new TipArea(364,672,377,688,
               "OSB 9 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[0].addTipArea(
            new TipArea(364,695,377,712,
               "OSB 10 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[0].addTipArea(
            new TipArea(317,747,331,761,
               "OSB 11 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[0].addTipArea(
            new TipArea(291,747,305,761,
               "OSB 12 Quick Select (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[0].addTipArea(
            new TipArea(264,747,279,761,
               "OSB 13 Quick Select (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[0].addTipArea(
            new TipArea(238,747,252,761,
               "OSB 14 Quick Select (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[0].addTipArea(
            new TipArea(211,746,225,761,
               "OSB 15 SWAP (Option Select Button)",
               "Swaps the content of the left and right MFDs"));

         mPositions[0].addTipArea(
            new TipArea(162,695,178,712,
               "OSB 16 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[0].addTipArea(
            new TipArea(162,672,178,688,
               "OSB 17 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[0].addTipArea(
            new TipArea(162,648,178,665,
               "OSB 18 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[0].addTipArea(
            new TipArea(162,625,178,641,
               "OSB 19 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[0].addTipArea(
            new TipArea(162,600,178,617,
               "OSB 20 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         // right MFD OSB buttons

         int deltaXMFD = 478;

         mPositions[0].addTipArea(
            new TipArea(211+deltaXMFD,549,225+deltaXMFD,564,
               "OSB 1 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[0].addTipArea(
            new TipArea(238+deltaXMFD,549,252+deltaXMFD,564,
               "OSB 2 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[0].addTipArea(
            new TipArea(264+deltaXMFD,549,279+deltaXMFD,564,
               "OSB 3 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[0].addTipArea(
            new TipArea(291+deltaXMFD,549,305+deltaXMFD,564,
               "OSB 4 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[0].addTipArea(
            new TipArea(317+deltaXMFD,549,331+deltaXMFD,564,
               "OSB 5 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[0].addTipArea(
            new TipArea(364+deltaXMFD,600,377+deltaXMFD,617,
               "OSB 6 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[0].addTipArea(
            new TipArea(364+deltaXMFD,625,377+deltaXMFD,641,
               "OSB 7 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[0].addTipArea(
            new TipArea(364+deltaXMFD,648,377+deltaXMFD,665,
               "OSB 8 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[0].addTipArea(
            new TipArea(364+deltaXMFD,672,377+deltaXMFD,688,
               "OSB 9 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[0].addTipArea(
            new TipArea(364+deltaXMFD,695,377+deltaXMFD,712,
               "OSB 10 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[0].addTipArea(
            new TipArea(317+deltaXMFD,747,331+deltaXMFD,761,
               "OSB 11 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[0].addTipArea(
            new TipArea(291+deltaXMFD,747,305+deltaXMFD,761,
               "OSB 12 Quick Select (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[0].addTipArea(
            new TipArea(264+deltaXMFD,747,279+deltaXMFD,761,
               "OSB 13 Quick Select (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[0].addTipArea(
            new TipArea(238+deltaXMFD,747,252+deltaXMFD,761,
               "OSB 14 Quick Select (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[0].addTipArea(
            new TipArea(211+deltaXMFD,746,225+deltaXMFD,761,
               "OSB 15 SWAP (Option Select Button)",
               "Swaps the content of the left and right MFDs"));

         mPositions[0].addTipArea(
            new TipArea(162+deltaXMFD,695,178+deltaXMFD,712,
               "OSB 16 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[0].addTipArea(
            new TipArea(162+deltaXMFD,672,178+deltaXMFD,688,
               "OSB 17 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[0].addTipArea(
            new TipArea(162+deltaXMFD,648,178+deltaXMFD,665,
               "OSB 18 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[0].addTipArea(
            new TipArea(162+deltaXMFD,625,178+deltaXMFD,641,
               "OSB 19 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[0].addTipArea(
            new TipArea(162+deltaXMFD,600,178+deltaXMFD,617,
               "OSB 20 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[0].addTipArea(
            new TipArea(377,131,409,217,
               "Airspeed (current airspeed in knots (nautical miles per hour))",
               ""));

         mPositions[0].addTipArea(
            new TipArea(593,88,612,209,
               "Altitude (current altitude in feet)",
               "either barometric above sea level, above ground level using radar, or automatically choooses based on height."));

         mPositions[0].addTipArea(
            new TipArea(560,225,610,239,
               "Altitude Low Indicator (Current AGL altitude in feet displayed)",
               "Flashing AL appears when altitude is below the ALOW setting (set using the ICP ALOW btn)"));

         mPositions[0].addTipArea(
            new TipArea(554,99,566,117,
               "Tadpole (Points you to the current steerpoint)",
               "Always present while in NAV mode.  Tail points in the direction of the current steerpoint.  Takes altitude into account."));

         mPositions[0].addTipArea(
            new TipArea(465,71,547,108,
               "Heading Indicator (Displays current compass heading)",
               "0 - North, 90 - East, 180 - South, 270 - East"));

         mPositions[0].addTipArea(
            new TipArea(454,120,558,192,
               "Pitch Ladder (indicates angle of climb/dive)",
               "Marked in 5 deg increments; 2.5 deg line also shown when gear is down; Tick marks on line-ends point to level flight; May slide left/right due to wind"));

         mPositions[0].addTipArea(
            new TipArea(412,122,433,137,
               "G Force Indicator",
               "G force you are currently experiencing; this is 1.0 when straight and level; F-16 limit is 9Gs"));

         mPositions[0].addTipArea(
            new TipArea(399,231,417,241,
               "Max G Force Indicator",
               "Highest G Force you have experienced during the current mission.  Automatically reset to 1.0 before takeoff."));

         mPositions[0].addTipArea(
            new TipArea(484,194,494,228,
               "Angle of Attack Bracket",
               "Position of the Flight Path Marker on this indicates AoA; If FPM is at the top, AoA = 11 deg, at the bottom AoA = 15 deg"));

         mPositions[0].addTipArea(
            new TipArea(399,242,418,253,
               "HUD Mode Indicator",
               "Indicates which HUD mode is being displayed",
               mFigures[mFigureHUDModes],10,50));

         poly = new Polygon();
         poly.addPoint(38,549);
         poly.addPoint(51,532);
         poly.addPoint(71,548);
         poly.addPoint(56,565);
         poly.addPoint(56,565);
         mPositions[0].addTipArea(
         new TipArea(poly,
            "F-ACK",
            "Cycles through faults on the Pilot Fault List (PFL) display"));

         mPositions[0].addTipArea(
            new TipArea(359,361,654,575,
               "ICP (Integrated Control Panel)",
               "Gives you control over frequently used comm/nav functions; Used along with the DED Display"));

         mPositions[0].addTipArea(
            new TipArea(349,65,668,348,
               "HUD (Head-Up Display)",
               "Provides info from the nav system, Fire control Radar (FCR), and Fire Control Computer (FCC)"));

         //************************************************************************
         // tip areas (POSITION 8)
         //************************************************************************

         // pit position 8 (one up from 0) contains some of the same tip areas we already added
         // just shifted down.  so, we clone the tip areas from position 0 to position 8 at an offset
         int offsetX = 0;
         int offsetY = 333;
         Vector clones = mPositions[0].cloneTipAreas(offsetX,offsetY);
         for (int i = 0; i < clones.size(); i++)
         {
            mPositions[8].addTipArea((TipArea)clones.get(i));
         }

         //************************************************************************
         // tip areas (POSITION 16)
         //************************************************************************

         // clone'd at an offset from 0 just as was done with #8 above
         offsetX = 0;
         offsetY = -322;
         clones = mPositions[0].cloneTipAreas(offsetX,offsetY);
         for (int i = 0; i < clones.size(); i++)
         {
            mPositions[16].addTipArea((TipArea)clones.get(i));
         }
         clones = mPositions[0].cloneActivePanels(offsetX,offsetY);
         for (int j = 0; j < clones.size(); j++)
         {
            mPositions[16].addActivePanel((ActivePanel)clones.get(j));
         }

         mPositions[16].addTipArea(
            new TipArea(47,471,137,521,
               "Auto Pilot Controls (Roll and Pitch switches)",
               "AP will disengage if: refuel door open, gear down, FLCS fault, attitude exceeds +/- 60 degs of level, alt > 40k ft, airspeed > .95 mach",
               mFigures[mFigureAPSwitches],10,50));

         mPositions[16].addTipArea(
            new TipArea(395,438,445,549,
               "Angle of Attack (AoA) Indicator",
               "Range +/- 32 degs; Color coding corresponds to AOA Indexer (on left side of HUD)"));

         mPositions[16].addTipArea(
            new TipArea(465,438,564,549,
               "Attitude Direction Indicator (ADI)",
               "Displays pitch and roll of the aircraft; Within 10nm of the runway white ILS bars will also be shown."));

         mPositions[16].addTipArea(
            new TipArea(575,438,624,549,
               "Vertical Velocity Indicator",
               "Climb/Descent rate in feet per minute; black - descending, white - climbing"));

         mPositions[16].addTipArea(
            new TipArea(510,628,41,
               "Horizontal Situation Indicator (HSI) Compass Card",
               "Displays relative position/orientation to a TACAN, steerpoint, runway location, tanker, or markpoint."));

         mPositions[16].addTipArea(
            new TipArea(557,661,10,
               "Course Set Knob",
               "Use to dial in the desired course for the HSI."));

         mPositions[16].addTipArea(
            new TipArea(468,661,10,
               "Heading Set Knob",
               "Rotates the heading marker in the HSI to the desired location."));

         mPositions[16].addTipArea(
            new TipArea(462,575,484,587,
               "Range Indicator",
               "range in nm to selected TACAN, or steerpoint; Red flag indicates invalid data"));

         mPositions[16].addTipArea(
            new TipArea(536,575,560,587,
               "Course Indicator",
               "Displays the course dialed in via the Course Set Knob"));

         mPositions[16].addTipArea(
            new TipArea(388,562,453,623,
               "Instr Mode Selector (Source Used for HSI Display)",
               "NAV - steerpoint, ILS/NAV - runway, TCN - tanker/airbase w/TACAN, ILS/TCN - airbase w/manual TACAN"));

         poly = new Polygon();
         poly.addPoint(573,585);
         poly.addPoint(637,585);
         poly.addPoint(636,616);
         poly.addPoint(631,649);
         poly.addPoint(573,649);
         mPositions[16].addTipArea(
            new TipArea(poly,
               "Fuel Qty Selector (Controls Fuel Quantity Gauge)",
               "TEST - 6000 gal, NORM - total/indiv tanks, RESV - reservoirs, INT WING/EXT WING/EXT CENTER - indiv tanks"));

         mPositions[16].addTipArea(
            new TipArea(573,650,623,708,
               "Fuel Transfer control (ext. tank usage)",
               "NORM - centerline tank first then wing tanks, WING FIRST - wing tanks first then centerline tank"));

         mPositions[16].addTipArea(
            new TipArea(0,584,12,636,
               "Emergency Jettison",
               "Jettison all A-G stores.  Should only be done in an emergency."));

         mPositions[16].addTipArea(
            new TipArea(15,552,92,643,
               "Landing Gear Lights",
               "Lit when gear is down, off when gear is up or damaged."));

         mPositions[16].addTipArea(
            new TipArea(93,530,138,604,
               "Tail Hook Release",
               "Used for landing in emergency situations"));

         mPositions[16].addTipArea(
            new TipArea(0,643,20,701,
               "Parking Brake",
               "Use to toggle parking brake on/off"));

         mPositions[16].addTipArea(
            new TipArea(72,651,122,764,
               "Landing Gear Handle",
               "Use to retract/lower the landing gear"));

         mPositions[16].addTipArea(
            new TipArea(0,720,50,764,
               "Landing Lights toggle",
               ""));

         mPositions[16].addTipArea(
            new TipArea(134,629,388,768,
               "Left Kneeboard",
               "Provides flight and steerpoint info, briefing, and a moving map with the flight plan overlaid"));

         poly = new Polygon();
         poly.addPoint(622,768);
         poly.addPoint(637,628);
         poly.addPoint(878,657);
         poly.addPoint(867,768);
         mPositions[16].addTipArea(
            new TipArea(poly,
               "Right kneeboard",
               "threat symbology and ranges; click \"REFUELING\" to see a refueling guide."));

         mPositions[16].addTipArea(
            new TipArea(902,559,977,625,
               "Compass",
               ""));

         mPositions[16].addTipArea(
            new TipArea(1022,593,38,
               "Fuel Gauge",
               "Digital Display shows total remaining fuel, hands on the analog display show indiv tank levels"));

         mPositions[16].addTipArea(
            new TipArea(902,708,941,721,
               "FLCS FAULT",
               "Failure in the FLCS Control System"));

         mPositions[16].addTipArea(
            new TipArea(902,722,941,737,
               "ELEC SYS",
               "A failure in the electrical system.  Check the electrical panel for more information."));

         mPositions[16].addTipArea(
            new TipArea(902,738,941,752,
               "PROBE HEAT",
               "Probe Heat not working; This may result in invalid airspeed display."));

         mPositions[16].addTipArea(
            new TipArea(902,753,941,768,
               "CADC",
               "Central Air Data Computer has a problem"));

         int r2offsetX = 42;
         int r2offsetY = -1;

         mPositions[16].addTipArea(
            new TipArea(902+r2offsetX,708+r2offsetY,941+r2offsetX,721+r2offsetY,
               "ENGINE FAULT",
               "Indicates a loss of valid data to the engine.  Will result in loss of some or all engine capabilities."));

         mPositions[16].addTipArea(
            new TipArea(902+r2offsetX,722+r2offsetY,941+r2offsetX,737+r2offsetY,
               "SEC",
               "You're running in secondary engine controls"));

         mPositions[16].addTipArea(
            new TipArea(902+r2offsetX,738+r2offsetY,941+r2offsetX,752+r2offsetY,
               "FUEL OIL HOT",
               "The fuel oil is too hot - duh!"));

         mPositions[16].addTipArea(
            new TipArea(902+r2offsetX,753+r2offsetY,941+r2offsetX,768+r2offsetY,
               "INLET ICING",
               "Ice Detected on the Engine Inlet"));

         r2offsetX += 42;
         r2offsetY += -1;

         mPositions[16].addTipArea(
            new TipArea(902+r2offsetX,708+r2offsetY,941+r2offsetX,721+r2offsetY,
               "AVIONICS FAULT",
               "General Fault with the System Avionics or FLCS"));

         mPositions[16].addTipArea(
            new TipArea(902+r2offsetX,722+r2offsetY,941+r2offsetX,737+r2offsetY,
               "EQUIP HOT",
               "Some of the avionics equipment is not being cooled sufficiently"));

         mPositions[16].addTipArea(
            new TipArea(902+r2offsetX,738+r2offsetY,941+r2offsetX,752+r2offsetY,
               "RADAR ALT",
               "Malfunction in the radar altimeter.  You'll need to rely on barometric altitude."));

         mPositions[16].addTipArea(
            new TipArea(902+r2offsetX,753+r2offsetY,941+r2offsetX,768+r2offsetY,
               "IFF (Identify Friend or Foe)",
               "Other Aircraft may not be able to identify you electronically."));

         mPositions[16].addTipArea(
            new TipArea(932,484,42,
               "FTIT (Fan Turbine Inlet Temperature)",
               "Shows how hot the engine is running.  If this is starting to redline then you're in trouble."));

         poly = new Polygon();
         poly.addPoint(38,229);
         poly.addPoint(52,210);
         poly.addPoint(73,226);
         poly.addPoint(56,244);
         poly.addPoint(56,244);
         mPositions[16].addTipArea(
            new TipArea(poly,
            "F-ACK",
            "Cycles through faults on the Pilot Fault List (PFL) display"));

         mPositions[16].addTipArea(
            new TipArea(914,638,1024,673,
               "Pilot Fault List (PFL)",
               "Displays faults (subsystem/function/severity); Cycle through entries using left-eyebrow F-ACK button",
               mFigures[mFigurePFL],10,50));

         mPositions[16].addTipArea(
            new TipArea(508,715,32,
               "Cockpit Air Vent",
               "Twist to open for air flow into the cockpit; Adjust for desired comfort level."));

         //************************************************************************
         // tip areas (POSITION 23)
         //************************************************************************

         mPositions[23].addTipArea(
            new TipArea(363,0,391,36,
               "Increment/Decrement",
               "Used to cycle through values on the DED.  Look for the value on the DED with the small arrows."));

         mPositions[23].addTipArea(
            new TipArea(413,0,450,24,
               "RETURN Data Command Switch (DCS)",
               "Returns the DED back to the default (CNI) page from any other page."));

         mPositions[23].addTipArea(
            new TipArea(448,28,481,49,
               "DOWN Data Command Switch (DCS)",
               "Cycle Backwards through editable options on the DED"));

         mPositions[23].addTipArea(
            new TipArea(483,0,513,22,
               "SEQ Data Command Switch (DCS)",
               "Cycle through subpages and options on the DED"));

         mPositions[23].addTipArea(
            new TipArea(522,22,583,45,
               "MAN RESET",
               "Cancels any pending warning messages"));

         // left MFD

         mPositions[23].addTipArea(
            new TipArea(49,104,282,342,
               "Left MFD",
               "Use '[' to cycle displays"));

         mPositions[23].addTipArea(
            new TipArea(77,66,99,88,
               "OSB 1 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[23].addTipArea(
            new TipArea(116,66,139,88,
               "OSB 2 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[23].addTipArea(
            new TipArea(156,66,178,88,
               "OSB 3 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[23].addTipArea(
            new TipArea(195,66,217,88,
               "OSB 4 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[23].addTipArea(
            new TipArea(234,66,256,88,
               "OSB 5 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         int deltaY = 294;

         mPositions[23].addTipArea(
            new TipArea(77,66+deltaY,99,88+deltaY,
               "OSB 15 SWAP (Option Select Button)",
               "Swaps the content of the left and right MFDs"));

         mPositions[23].addTipArea(
            new TipArea(116,66+deltaY,139,88+deltaY,
               "OSB 14 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[23].addTipArea(
            new TipArea(156,66+deltaY,178,88+deltaY,
               "OSB 13 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[23].addTipArea(
            new TipArea(195,66+deltaY,217,88+deltaY,
               "OSB 12 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[23].addTipArea(
            new TipArea(234,66+deltaY,256,88+deltaY,
               "OSB 11 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[23].addTipArea(
            new TipArea(5,144,27,166,
               "OSB 20 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[23].addTipArea(
            new TipArea(5,180,27,201,
               "OSB 19 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[23].addTipArea(
            new TipArea(5,215,27,236,
               "OSB 18 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[23].addTipArea(
            new TipArea(5,251,27,272,
               "OSB 17 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[23].addTipArea(
            new TipArea(6,285,27,306,
               "OSB 16 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         int deltaX = 300;

         mPositions[23].addTipArea(
            new TipArea(5+deltaX,144,27+deltaX,166,
               "OSB 6 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[23].addTipArea(
            new TipArea(5+deltaX,180,27+deltaX,201,
               "OSB 7 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[23].addTipArea(
            new TipArea(5+deltaX,215,27+deltaX,236,
               "OSB 8 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[23].addTipArea(
            new TipArea(5+deltaX,251,27+deltaX,272,
               "OSB 9 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[23].addTipArea(
            new TipArea(6+deltaX,285,27+deltaX,306,
               "OSB 10 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         // right MFD

         deltaXMFD = 690;

         mPositions[23].addTipArea(
            new TipArea(49+deltaXMFD,104,282+deltaXMFD,342,
               "Right MFD",
               "Use ']' to cycle displays"));

         mPositions[23].addTipArea(
            new TipArea(77+deltaXMFD,66,99+deltaXMFD,88,
               "OSB 1 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[23].addTipArea(
            new TipArea(116+deltaXMFD,66,139+deltaXMFD,88,
               "OSB 2 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[23].addTipArea(
            new TipArea(156+deltaXMFD,66,178+deltaXMFD,88,
               "OSB 3 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[23].addTipArea(
            new TipArea(195+deltaXMFD,66,217+deltaXMFD,88,
               "OSB 4 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[23].addTipArea(
            new TipArea(234+deltaXMFD,66,256+deltaXMFD,88,
               "OSB 5 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         deltaY = 294;

         mPositions[23].addTipArea(
            new TipArea(77+deltaXMFD,66+deltaY,99+deltaXMFD,88+deltaY,
               "OSB 15 SWAP (Option Select Button)",
               "Swaps the content of the left and right MFDs"));

         mPositions[23].addTipArea(
            new TipArea(116+deltaXMFD,66+deltaY,139+deltaXMFD,88+deltaY,
               "OSB 14 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[23].addTipArea(
            new TipArea(156+deltaXMFD,66+deltaY,178+deltaXMFD,88+deltaY,
               "OSB 13 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[23].addTipArea(
            new TipArea(195+deltaXMFD,66+deltaY,217+deltaXMFD,88+deltaY,
               "OSB 12 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[23].addTipArea(
            new TipArea(234+deltaXMFD,66+deltaY,256+deltaXMFD,88+deltaY,
               "OSB 11 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[23].addTipArea(
            new TipArea(5+deltaXMFD,144,27+deltaXMFD,166,
               "OSB 20 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[23].addTipArea(
            new TipArea(5+deltaXMFD,180,27+deltaXMFD,201,
               "OSB 19 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[23].addTipArea(
            new TipArea(5+deltaXMFD,215,27+deltaXMFD,236,
               "OSB 18 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[23].addTipArea(
            new TipArea(5+deltaXMFD,251,27+deltaXMFD,272,
               "OSB 17 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[23].addTipArea(
            new TipArea(6+deltaXMFD,285,27+deltaXMFD,306,
               "OSB 16 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         deltaX = 300;

         mPositions[23].addTipArea(
            new TipArea(5+deltaX+deltaXMFD,144,27+deltaX+deltaXMFD,166,
               "OSB 6 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[23].addTipArea(
            new TipArea(5+deltaX+deltaXMFD,180,27+deltaX+deltaXMFD,201,
               "OSB 7 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[23].addTipArea(
            new TipArea(5+deltaX+deltaXMFD,215,27+deltaX+deltaXMFD,236,
               "OSB 8 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[23].addTipArea(
            new TipArea(5+deltaX+deltaXMFD,251,27+deltaX+deltaXMFD,272,
               "OSB 9 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[23].addTipArea(
            new TipArea(6+deltaX+deltaXMFD,285,27+deltaX+deltaXMFD,306,
               "OSB 10 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[23].addTipArea(
            new TipArea(419,229,78,
               "Airspeed/Mach Disk",
               "Current Airspeed (80 to 800 knots) on outside dial, Mach Factor (speed relative to the speed of sound)"));

         mPositions[23].addTipArea(
            new TipArea(594,224,78,
               "Barometric Altimeter",
               "Displays altitude above sea level (which may not be the same as altitude above ground level)"));

         mPositions[23].addTipArea(
            new TipArea(342,330,421,498,
               "Angle of Attack (AoA) Indicator",
               "Range +/- 32 degs; Color coding corresponds to AOA Indexer (on left side of HUD)"));

         mPositions[23].addTipArea(
            new TipArea(430,334,591,497,
               "Attitude Direction Indicator (ADI)",
               "Displays pitch and roll of the aircraft; Within 10nm of the runway white ILS bars will also be shown."));

         mPositions[23].addTipArea(
            new TipArea(597,331,671,493,
               "Vertical Velocity Indicator",
               "Climb/Descent rate in feet per minute; black - descending, white - climbing"));

         mPositions[23].addTipArea(
            new TipArea(511,602,60,
               "Horizontal Situation Indicator (HSI) Compass Card",
               "Displays relative position/orientation to a TACAN, steerpoint, runway location, tanker, or markpoint."));

         mPositions[23].addTipArea(
            new TipArea(450,645,13,
               "Heading Set Knob",
               "Rotates the heading marker in the HSI to the desired location."));

         mPositions[23].addTipArea(
            new TipArea(574,645,13,
               "Course Set Knob",
               "Use to dial in the desired course for the HSI."));

         mPositions[23].addTipArea(
            new TipArea(338,509,424,588,
               "Instr Mode Selector (Source Used for HSI Display)",
               "NAV - steerpoint, ILS/NAV - runway, TCN - tanker/airbase w/TACAN, ILS/TCN - airbase w/manual TACAN"));

         mPositions[23].addTipArea(
            new TipArea(443,525,469,539,
               "Range Indicator",
               "range in nm to selected TACAN, or steerpoint; Red flag indicates invalid data"));

         mPositions[23].addTipArea(
            new TipArea(545,525,574,538,
               "Course Indicator",
               "Displays the course dialed in via the Course Set Knob"));

         mPositions[23].addTipArea(
            new TipArea(610,542,685,628,
               "Fuel Qty Selector (Controls Fuel Quantity Gauge)",
               "TEST - 6000 gal, NORM - total/indiv tanks, RESV - reservoirs, INT WING/EXT WING/EXT CENTER - indiv tanks"));

         mPositions[23].addTipArea(
            new TipArea(609,643,684,715,
               "Fuel Transfer control (ext. tank usage)",
               "NORM - centerline tank first then wing tanks, WING FIRST - wing tanks first then centerline tank"));

         mPositions[23].addTipArea(
            new TipArea(2,666,327,737,
               "Left Kneeboard",
               "Provides flight and steerpoint info, briefing, and a moving map with the flight plan overlaid"));

         mPositions[23].addTipArea(
            new TipArea(690,614,1011,736,
               "Right kneeboard",
               "threat symbology and ranges; click \"REFUELING\" to see a refueling guide."));

         mPositions[23].addTipArea(
            new TipArea(506,728,43,
               "Cockpit Air Vent",
               "Twist to open for air flow into the cockpit; Adjust for desired comfort level."));

         //************************************************************************
         // tip areas (POSITION 15)
         //************************************************************************

         mPositions[15].addTipArea(
            new TipArea(941,468,990,524,
               "R/F (Allows switching off detectable emissions)",
               "Norm - Normal mode, Quiet - Radar in standby/emissions reduced, Silent - ALL emissions silenced (RADAR, CARA (RALT), TFR)"));

         mPositions[15].addTipArea(
            new TipArea(954,531,984,560,
               "ECM",
               "Lit if ECM is powered"));

         mPositions[15].addTipArea(
            new TipArea(910,537,948,582,
               "Laser Arm (Turns on FLIR pod's targeting laser)",
               "Laser Guided Bombs require this to be on to illuminate the target.  If the laser stops or radar lock is lost, wpn follows ballistic trajectory."));

         mPositions[15].addTipArea(
            new TipArea(954,578,980,610,
               "Alt Rel (Alternate Weapons Release)",
               ""));

         mPositions[15].addTipArea(
            new TipArea(925,624,982,670,
               "Master Arm (Controls Access to all Weapon Systems)",
               "Off - Inactive, Sim - Active but launch is prohibited, Arm - Fully Active (armed and dangerous)"));

         mPositions[15].addTipArea(
            new TipArea(882,710,976,740,
               "Auto Pilot Controls (Roll and Pitch switches)",
               "AP will disengage if: refuel door open, gear down, FLCS fault, attitude exceeds +/- 60 degs of level, alt > 40k ft, airspeed > .95 mach",
               mFigures[mFigureAPSwitches],10,50));

         mPositions[15].addTipArea(
            new TipArea(1007,522,1021,535,
               "OSB 20 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[15].addTipArea(
            new TipArea(1007,547,1020,560,
               "OSB 19 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[15].addTipArea(
            new TipArea(1008,572,1021,584,
               "OSB 18 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[15].addTipArea(
            new TipArea(1009,595,1022,607,
               "OSB 17 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[15].addTipArea(
            new TipArea(1008,618,1020,631,
               "OSB 16 (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         poly = new Polygon();
         poly.addPoint(960,368);
         poly.addPoint(994,322);
         poly.addPoint(1018,341);
         poly.addPoint(981,386);
         mPositions[15].addTipArea(
            new TipArea(poly,
               "Master Caution Light",
               "Indicates another caution light has been lit, click to clear it"));

         poly = new Polygon();
         poly.addPoint(934,451);
         poly.addPoint(913,433);
         poly.addPoint(929,412);
         poly.addPoint(950,431);
         mPositions[15].addTipArea(
            new TipArea(poly,
               "TF-FAIL (Left Eyebrow Warning Light)",
               "Indicates a failure in the terrain following system"));

         mPositions[15].addTipArea(
            new TipArea(982,410,1008,437,
               "NAVAL",
               "Press to increase pri of Naval threats which are normally lower pri than air or ground threats."));

         poly = new Polygon();
         poly.addPoint(883,472);
         poly.addPoint(897,451);
         poly.addPoint(918,467);
         poly.addPoint(902,487);
         poly.addPoint(902,487);
         mPositions[15].addTipArea(
            new TipArea(poly,
            "F-ACK",
            "Cycles through faults on the Pilot Fault List (PFL) display"));

         //************************************************************************
         // tip areas (POSITION 1)
         //************************************************************************

         poly = new Polygon();
         poly.addPoint(39,377);
         poly.addPoint(56,364);
         poly.addPoint(71,383);
         poly.addPoint(55,396);
         poly.addPoint(55,396);
         mPositions[1].addTipArea(
            new TipArea(poly,
               "ENG FIRE (Right Eyebrow Warning Light)",
               "If lit, there is a fire in your engine.  Your only recourse is to eject before your plane explodes (hold Ctrl-E)."));

         poly = new Polygon();
         poly.addPoint(57,402);
         poly.addPoint(76,386);
         poly.addPoint(90,407);
         poly.addPoint(72,421);
         poly.addPoint(72,421);
         mPositions[1].addTipArea(
            new TipArea(poly,
               "HYD/OIL PRESS (Right Eyebrow Warning Light)",
               "Indicates low pressure in hydraulic systems or low oil engine pressure.  Engine will eventually freeze up."));

         poly = new Polygon();
         poly.addPoint(76,427);
         poly.addPoint(93,411);
         poly.addPoint(109,433);
         poly.addPoint(91,446);
         poly.addPoint(91,446);
         mPositions[1].addTipArea(
            new TipArea(poly,
               "FLCS DBU ON (Right Eyebrow Warning Light)",
               "Indicates a problem with the dual flight control system (2 or more FLCS computers offline).  Significantly reduced flight control reliability.  RTB ASAP."));

         poly = new Polygon();
         poly.addPoint(101,449);
         poly.addPoint(114,438);
         poly.addPoint(128,455);
         poly.addPoint(111,471);
         poly.addPoint(111,471);
         mPositions[1].addTipArea(
            new TipArea(poly,
               "T/L CFG (Right Eyebrow Warning Light)",
               "Indicates your configuration is incorrect for takeoff/landing (gear not down)."));

         poly = new Polygon();
         poly.addPoint(116,476);
         poly.addPoint(132,465);
         poly.addPoint(145,482);
         poly.addPoint(127,495);
         poly.addPoint(127,495);
         mPositions[1].addTipArea(
            new TipArea(poly,
               "CANOPY (Right Eyebrow Warning Light)",
               "Indicates a problem with cockpit pressurization."));

         mPositions[1].addTipArea(
            new TipArea(64,510,22,
               "Oil Pressure (Engine Oil Pressure)",
               "Ranging from 0 to 100 pounds per square inch (psi).  If this drops below 15 psi, you have a serious oil pressure leak."));

         mPositions[1].addTipArea(
            new TipArea(70,571,32,
               "Nozzle Pos (Position of the Engine Nozzle)",
               "Mostly open at idle, closed at Mil power (100% RPM), and fully open at Afterburner"));

         mPositions[1].addTipArea(
            new TipArea(75,645,40,
               "RPM (Engine Revolutions Per Minute)",
               "Expressed as a percentage from 0% to 100%.  75% - idle, 100% - full military power (just before Afterburner kicks in)"));

         mPositions[1].addTipArea(
            new TipArea(87,728,41,
               "FTIT (Fan Turbine Inlet Temperature)",
               "Shows how hot the engine is running.  If this is starting to redline then you're in trouble."));

         mPositions[1].addTipArea(
            new TipArea(0,526,13,540,
               "OSB 6 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[1].addTipArea(
            new TipArea(0,550,13,564,
               "OSB 7 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[1].addTipArea(
            new TipArea(1,573,14,588,
               "OSB 8 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[1].addTipArea(
            new TipArea(1,596,12,610,
               "OSB 9 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[1].addTipArea(
            new TipArea(1,621,13,634,
               "OSB 10 (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         //************************************************************************
         // tip areas (POSITION 17)
         //************************************************************************

         mPositions[17].addTipArea(
            new TipArea(1,1,28,136,
               "Angle of Attack (AoA) Indicator",
               "Range +/- 32 degs; Color coding corresponds to AOA Indexer (on left side of HUD)"));

         mPositions[17].addTipArea(
            new TipArea(36,1,184,137,
               "Attitude Direction Indicator (ADI)",
               "Displays pitch and roll of the aircraft; Within 10nm of the runway white ILS bars will also be shown."));

         mPositions[17].addTipArea(
            new TipArea(194,0,256,134,
               "Vertical Velocity Indicator",
               "Climb/Descent rate in feet per minute; black - descending, white - climbing"));

         mPositions[17].addTipArea(
            new TipArea(316,11,334,29,
               "OSB 15 SWAP (Option Select Button)",
               "Swaps the content of the left and right MFDs"));

         mPositions[17].addTipArea(
            new TipArea(351,12,370,29,
               "OSB 14 Quick Select (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[17].addTipArea(
            new TipArea(388,11,406,28,
               "OSB 13 Quick Select (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[17].addTipArea(
            new TipArea(423,9,441,29,
               "OSB 12 Quick Select (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[17].addTipArea(
            new TipArea(460,9,478,28,
               "OSB 11 Quick Select (Option Select Button)",
               "Function depends on which display is active (cycle displays with ']')"));

         mPositions[17].addTipArea(
            new TipArea(590,50,44,
               "FTIT (Fan Turbine Inlet Temperature)",
               "Shows how hot the engine is running.  If this is starting to redline then you're in trouble."));

         mPositions[17].addTipArea(
            new TipArea(0,158,33,224,
               "Instr Mode Selector (Source Used for HSI Display)",
               "NAV - steerpoint, ILS/NAV - runway, TCN - tanker/airbase w/TACAN, ILS/TCN - airbase w/manual TACAN"));

         mPositions[17].addTipArea(
            new TipArea(48,167,70,177,
               "Range Indicator",
               "range in nm to selected TACAN, or steerpoint; Red flag indicates invalid data"));

         mPositions[17].addTipArea(
            new TipArea(146,165,171,176,
               "Course Indicator",
               "Displays the course dialed in via the Course Set Knob"));

         mPositions[17].addTipArea(
            new TipArea(112,236,53,
               "Horizontal Situation Indicator (HSI) Compass Card",
               "Displays relative position/orientation to a TACAN, steerpoint, runway location, tanker, or markpoint."));

         mPositions[17].addTipArea(
            new TipArea(56,277,12,
               "Heading Set Knob",
               "Rotates the heading marker in the HSI to the desired location."));

         mPositions[17].addTipArea(
            new TipArea(170,277,13,
               "Course Set Knob",
               "Use to dial in the desired course for the HSI."));

         mPositions[17].addTipArea(
            new TipArea(193,183,267,264,
               "Fuel Qty Selector (Controls Fuel Quantity Gauge)",
               "TEST - 6000 gal, NORM - total/indiv tanks, RESV - reservoirs, INT WING/EXT WING/EXT CENTER - indiv tanks"));

         mPositions[17].addTipArea(
            new TipArea(200,280,267,342,
               "Fuel Transfer control (ext. tank usage)",
               "NORM - centerline tank first then wing tanks, WING FIRST - wing tanks first then centerline tank"));

         poly = new Polygon();
         poly.addPoint(287,208);
         poly.addPoint(531,229);
         poly.addPoint(501,566);
         poly.addPoint(255,544);
         poly.addPoint(255,544);
         mPositions[17].addTipArea(
            new TipArea(poly,
               "Right kneeboard",
               "threat symbology and ranges; click \"REFUELING\" to see a refueling guide."));

         mPositions[17].addTipArea(
            new TipArea(4,438,234,517,
               "Ejection Lever",
               "Click and hold for several seconds to eject."));

         mPositions[17].addTipArea(
            new TipArea(558,133,639,201,
               "Magnetic Compass",
               ""));

         mPositions[17].addTipArea(
            new TipArea(684,163,43,
               "Fuel Gauge",
               "Digital Display shows total remaining fuel, hands on the analog display show indiv tank levels"));

         mPositions[17].addTipArea(
            new TipArea(559,287,596,298,
               "FLCS FAULT",
               "Failure in the FLCS Control System"));

         mPositions[17].addTipArea(
            new TipArea(559,303,597,316,
               "ELEC SYS",
               "A failure in the electrical system.  Check the electrical panel for more information."));

         mPositions[17].addTipArea(
            new TipArea(561,320,597,331,
               "PROBE HEAT",
               "Probe Heat not working; This may result in invalid airspeed display."));

         mPositions[17].addTipArea(
            new TipArea(560,336,596,346,
               "CADC",
               "Central Air Data Computer has a problem"));

         mPositions[17].addTipArea(
            new TipArea(561,352,596,363,
               "STORES CONFIG",
               "The Cat I/III stores setting is wrong for the stores loaded; or, you've used up your weapons."));

         mPositions[17].addTipArea(
            new TipArea(561,369,597,379,
               "",
               ""));

         mPositions[17].addTipArea(
            new TipArea(560,383,595,394,
               "FWD FUEL LOW",
               "400 lbs or less remaining in the front tanks; in air re-fueling will not turn this off."));

         mPositions[17].addTipArea(
            new TipArea(560,400,595,413,
               "AFT FUEL LOW",
               "250 lbs or less remaining in the front tanks; in air re-fueling will not turn this off."));

         mPositions[17].addTipArea(
            new TipArea(603,286,639,297,
               "ENGINE FAULT",
               "Indicates a loss of valid data to the engine.  Will result in loss of some or all engine capabilities."));

         mPositions[17].addTipArea(
            new TipArea(603,303,638,313,
               "SEC",
               "You're running in secondary engine controls"));

         mPositions[17].addTipArea(
            new TipArea(602,319,638,329,
               "FUEL OIL HOT",
               "The fuel oil is too hot - duh!"));

         mPositions[17].addTipArea(
            new TipArea(603,335,638,345,
               "INLET ICING",
               "Ice Detected on the Engine Inlet"));

         mPositions[17].addTipArea(
            new TipArea(603,350,638,361,
               "OVERHEAT",
               "Engine overheat condition; reduce to minimum allowable throttle and land ASAP; engine will eventually explode."));

         mPositions[17].addTipArea(
            new TipArea(604,367,637,376,
               "EEC",
               "Alternator has failed"));

         mPositions[17].addTipArea(
            new TipArea(603,383,636,391,
               "BUC",
               "Engine running on backup fuel control system; Use caution with throttle controls."));

         mPositions[17].addTipArea(
            new TipArea(603,399,637,408,
               "",
               ""));

         mPositions[17].addTipArea(
            new TipArea(645,286,680,296,
               "AVIONICS FAULT",
               "General Fault with the System Avionics or FLCS"));

         mPositions[17].addTipArea(
            new TipArea(645,302,679,313,
               "EQUIP HOT",
               "Some of the avionics equipment is not being cooled sufficiently"));

         mPositions[17].addTipArea(
            new TipArea(645,317,678,328,
               "RADAR ALT",
               "Malfunction in the radar altimeter.  You'll need to rely on barometric altitude."));

         mPositions[17].addTipArea(
            new TipArea(645,334,680,344,
               "IFF (Identify Friend or Foe)",
               "Other Aircraft may not be able to identify you electronically."));

         mPositions[17].addTipArea(
            new TipArea(646,350,679,360,
               "NUCLEAR",
               "Problems in the nuclear release circuitry"));

         mPositions[17].addTipArea(
            new TipArea(645,367,679,376,
               "",
               ""));

         mPositions[17].addTipArea(
            new TipArea(645,382,679,391,
               "",
               ""));

         mPositions[17].addTipArea(
            new TipArea(646,398,679,408,
               "",
               ""));

         mPositions[17].addTipArea(
            new TipArea(687,283,721,293,
               "SEAT NOT ARMED",
               "The ejector seat system is not aremd."));

         mPositions[17].addTipArea(
            new TipArea(687,300,722,311,
               "NWS FAIL",
               "Nose Wheel Steering system has failed; you will not be able to steer while on the ground."));

         mPositions[17].addTipArea(
            new TipArea(687,317,721,327,
               "ANTI SKID",
               "The anti skid on the braking system is not functional"));

         mPositions[17].addTipArea(
            new TipArea(686,333,721,343,
               "HOOK",
               "Hook is used in emergency landings; when lit, the hook is not up and locked."));

         mPositions[17].addTipArea(
            new TipArea(687,349,722,358,
               "OXY LOW",
               "Onboard oxygen system is running low"));

         mPositions[17].addTipArea(
            new TipArea(685,365,723,375,
               "CABIN PRESS",
               "Low cabin pressure; Since you're wearing a mask this shouldn't affect you"));

         mPositions[17].addTipArea(
            new TipArea(688,380,721,390,
               "",
               ""));

         mPositions[17].addTipArea(
            new TipArea(688,396,723,410,
               "",
               ""));

         mPositions[17].addTipArea(
            new TipArea(746,251,21,
               "Hydraulic Pressure A",
               "Pressure in Hydraulic System A; A and B are redundant hydraulic systems.  Eject if both fail.",
               mFigures[mFigureHydraulicA],10,50));

         mPositions[17].addTipArea(
            new TipArea(774,286,19,
               "Hydraulic Pressure B",
               "Pressure in Hydraulic System B; A and B are redundant hydraulic systems.  Eject if both fail.",
               mFigures[mFigureHydraulicB],10,50));

         mPositions[17].addTipArea(
            new TipArea(769,333,27,
               "Oxygen Supply",
               "Amount of remaining oxygen"));

         mPositions[17].addTipArea(
            new TipArea(824,376,36,
               "EPU Fuel",
               "Amount of EPU fuel (hydrazine)"));

         mPositions[17].addTipArea(
            new TipArea(779,463,28,
               "Cabin Pressure",
               "Shows the effective cabin pressure"));

         mPositions[17].addTipArea(
            new TipArea(859,448,26,
               "Clock",
               "Shows the current time in the Falcon world (use CAPS lock and SHIFT-CAPS lock for time accelleration)"));

         mPositions[17].addTipArea(
            new TipArea(781,545,18,
               "Left HPT",
               "Supplies electrical power to the left fuselage hardpoint (for attached weapons)"));

         mPositions[17].addTipArea(
            new TipArea(823,537,21,
               "Right HPT",
               "Supplies electrical power to the right fuselage hardpoint (for attached weapons)"));

         mPositions[17].addTipArea(
            new TipArea(866,530,19,
               "FCR",
               "Powers the Fire Control Radar"));

         mPositions[17].addTipArea(
            new TipArea(908,528,19,
               "Radar Altimeter",
               "On/Off/Standby; Takes time to warm up, so keep it in standby to allow turning it back on quickly."));

         poly = new Polygon();
         poly.addPoint(938,551);
         poly.addPoint(988,619);
         poly.addPoint(1008,605);
         poly.addPoint(968,543);
         poly.addPoint(968,543);
         mPositions[17].addTipArea(
            new TipArea(poly,
               "Nuclear Consent",
               "System which allows nuclear weapons to be released"));

         mPositions[17].addTipArea(
            new TipArea(823,607,13,
               "Flight Path Marker (Display Modes for FPM)",
               "ATT - pitch ladder and FPM, FPM - only FPM, Off - Neither"));

         mPositions[17].addTipArea(
            new TipArea(870,601,14,
               "DED Data (HUD display of DED)",
               "DED Data - displayed on HUD, PFL - PFL list shown, Off - neither"));

         mPositions[17].addTipArea(
            new TipArea(917,591,18,
               "Manual Bombing (used for bombing if FCC fails)",
               "Off, Primary (Normal), and Standby (Backup)"));

         mPositions[17].addTipArea(
            new TipArea(856,685,18,
               "Radar Altimeter",
               "Off, On, or Standby; Keep in standby to allow quickly turning it back on."));

         mPositions[17].addTipArea(
            new TipArea(906,674,13,
               "HUD Brightness Control",
               "Set for day or night flying or set to automatically maintain appropriate brightness"));

         mPositions[17].addTipArea(
            new TipArea(953,664,14,
               "Test Step",
               "Displays various test patterns on the HUD"));

         poly = new Polygon();
         poly.addPoint(849,738);
         poly.addPoint(999,699);
         poly.addPoint(1013,725);
         poly.addPoint(1013,736);
         poly.addPoint(1013,736);
         mPositions[17].addTipArea(
            new TipArea(poly,
               "Cockpit Lighting",
               "Various Controls to adjust mood lighting in-cockpit"));

         mPositions[17].addTipArea(
            new TipArea(683,446,700,502,
               "Pitch Trim",
               "Use to increase/decrease pitch trim (using Left and Right mouse clicks)"));

         mPositions[17].addTipArea(
            new TipArea(661,466,715,485,
               "Roll Trim",
               "Use to increase/decrease roll trim (using Left and Right mouse clicks)"));

         mPositions[17].addTipArea(
            new TipArea(571,216,707,254,
               "Pilot Fault List (PFL)",
               "Displays faults (subsystem/function/severity); Cycle through entries using left-eyebrow F-ACK button",
               mFigures[mFigurePFL],10,50));

         mPositions[17].addTipArea(
            new TipArea(105,355,47,
               "Cockpit Air Vent",
               "Twist to open for air flow into the cockpit; Adjust for desired comfort level."));

         //************************************************************************
         // tip areas (POSITION 18)
         //************************************************************************

         mPositions[18].addTipArea(
            new TipArea(5,197,73,
               "EPU Fuel",
               "Amount of EPU fuel (hydrazine)"));

         mPositions[18].addTipArea(
            new TipArea(141,345,58,
               "Cabin Pressure",
               "Shows the effective cabin pressure"));

         mPositions[18].addTipArea(
            new TipArea(175,208,63,
               "Clock",
               "Shows the current time in the Falcon world (use CAPS lock and SHIFT-CAPS lock for time accelleration)"));

         mPositions[18].addTipArea(
            new TipArea(292,398,27,
               "Left HPT",
               "Supplies electrical power to the left fuselage hardpoint (for attached weapons)"));

         mPositions[18].addTipArea(
            new TipArea(300,344,24,
               "Right HPT",
               "Supplies electrical power to the right fuselage hardpoint (for attached weapons)"));

         mPositions[18].addTipArea(
            new TipArea(304,290,30,
               "FCR",
               "Powers the Fire Control Radar"));

         mPositions[18].addTipArea(
            new TipArea(307,232,27,
               "Radar Altimeter",
               "On/Off/Standby; Takes time to warm up, so keep it in standby to allow turning it back on quickly."));

         mPositions[18].addTipArea(
            new TipArea(377,430,21,
               "Scales (Altitude & Velocity Display)",
               "Off - velocity and altitude, VAH - all except vert. velocity, AH - all"));

         mPositions[18].addTipArea(
            new TipArea(377,359,23,
               "Flight Path Marker (Display Modes for FPM)",
               "ATT - pitch ladder and FPM, FPM - only FPM, Off - Neither"));

         mPositions[18].addTipArea(
            new TipArea(393,292,22,
               "DED Data (HUD display of DED)",
               "DED Data - displayed on HUD, PFL - PFL list shown, Off - neither"));

         mPositions[18].addTipArea(
            new TipArea(400,227,29,
               "Manual Bombing (used for bombing if FCC fails)",
               "Off, Primary (Normal), and Standby (Backup)"));

         mPositions[18].addTipArea(
            new TipArea(454,431,27,
               "HUD Airspeed Display",
               "CAS - Calibrated Airspeed, TAS - True Airspeed, GND SPD - Ground Speed"));

         mPositions[18].addTipArea(
            new TipArea(474,363,24,
               "Radar Altimeter",
               "Off, On, or Standby; Keep in standby to allow quickly turning it back on."));

         mPositions[18].addTipArea(
            new TipArea(463,297,25,
               "HUD Brightness Control",
               "Set for day or night flying or set to automatically maintain appropriate brightness"));

         mPositions[18].addTipArea(
            new TipArea(485,235,18,
               "Test Step",
               "Displays various test patterns on the HUD"));

         poly = new Polygon();
         poly.addPoint(510,487);
         poly.addPoint(528,208);
         poly.addPoint(594,205);
         poly.addPoint(606,227);
         poly.addPoint(758,303);
         poly.addPoint(763,501);
         poly.addPoint(763,501);
         mPositions[18].addTipArea(
            new TipArea(poly,
               "Cockpit Lighting",
               "Various Controls to adjust mood lighting in-cockpit"));

         mPositions[18].addTipArea(
            new TipArea(844,391,57,
               "Air Conditioning",
               "Allows you to maintain a pleasant temperature in the cockpit"));

         mPositions[18].addTipArea(
            new TipArea(841,249,57,
               "Air Source (for Cockpit/Fuel Tank Pressurization)",
               "Off - None, Norm - normal, Dump - dump cabin press but maintain tanks, Ram - ext tanks not pressurized"));

         mPositions[18].addTipArea(
            new TipArea(466,541,590,637,
               "Master Zeroize",
               "Quickly zero out loaded crypto keys (GPS/IFF); Use if ejecting over enemy territory"));

         mPositions[18].addTipArea(
            new TipArea(745,565,838,628,
               "VMS Inhibit",
               "Allows you to inhibit the voice message system"));

         mPositions[18].addTipArea(
            new TipArea(321,143,430,183,
               "Nuclear Consent",
               "System which allows nuclear weapons to be released"));

         //************************************************************************
         // tip areas (POSITION 19)
         //************************************************************************

         mPositions[19].addTipArea(
            new TipArea(398,224,31,
               "Anti Ice",
               "Switches on/off anti-icing systems which prevent ice buildup"));

         mPositions[19].addTipArea(
            new TipArea(459,193,597,262,
               "Antenna",
               "Controls which antenna to use for IFF and UHF comms"));

         mPositions[19].addTipArea(
            new TipArea(374,295,27,
               "FCC",
               "Power to the Fire Control Computer system"));

         mPositions[19].addTipArea(
            new TipArea(439,295,28,
               "SMS",
               "Power to the Stores Management system"));

         mPositions[19].addTipArea(
            new TipArea(504,296,28,
               "MFD",
               "Power to the MFDs"));

         mPositions[19].addTipArea(
            new TipArea(572,296,27,
               "UFC",
               "Power to the UFC system"));

         mPositions[19].addTipArea(
            new TipArea(393,403,33,
               "INS",
               "Power to the Inertial Navigation system (NORM for startup alignment; NAV for flying)"));

         mPositions[19].addTipArea(
            new TipArea(464,400,22,
               "GPS",
               "Power to the GPS system"));

         mPositions[19].addTipArea(
            new TipArea(518,402,22,
               "DL",
               "Power to the Datalink system"));

         mPositions[19].addTipArea(
            new TipArea(569,400,25,
               "MAP",
               "Power to the Map system"));

         mPositions[19].addTipArea(
            new TipArea(639,190,871,336,
               "Oxygen System",
               "Shows details of the onboard oxygen system"));

         mPositions[19].addTipArea(
            new TipArea(636,402,882,550,
               "DTU",
               "This is where the data cartridge is loaded"));

         //************************************************************************
         // tip areas (POSITION 20)
         //************************************************************************

         mPositions[20].addTipArea(
            new TipArea(346,23,446,81,
               "Auto Pilot Controls (Roll and Pitch switches)",
               "AP will disengage if: refuel door open, gear down, FLCS fault, attitude exceeds +/- 60 degs of level, alt > 40k ft, airspeed > .95 mach",
               mFigures[mFigureAPSwitches],10,50));

         mPositions[20].addTipArea(
            new TipArea(538,11,554,28,
               "OSB 15 SWAP (Option Select Button)",
               "Swaps the content of the left and right MFDs"));

         mPositions[20].addTipArea(
            new TipArea(573,9,591,27,
               "OSB 14 Quick Select (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[20].addTipArea(
            new TipArea(610,11,628,28,
               "OSB 13 Quick Select (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[20].addTipArea(
            new TipArea(646,13,664,30,
               "OSB 12 Quick Select (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[20].addTipArea(
            new TipArea(682,10,702,30,
               "OSB 11 Quick Select (Option Select Button)",
               "Function depends on which display is active (cycle displays with '[')"));

         mPositions[20].addTipArea(
            new TipArea(786,0,854,135,
               "Angle of Attack (AoA) Indicator",
               "Range +/- 32 degs; Color coding corresponds to AOA Indexer (on left side of HUD)"));

         mPositions[20].addTipArea(
            new TipArea(863,1,1011,135,
               "Attitude Direction Indicator (ADI)",
               "Displays pitch and roll of the aircraft; Within 10nm of the runway white ILS bars will also be shown."));

         mPositions[20].addTipArea(
            new TipArea(777,149,861,229,
               "Instr Mode Selector (Source Used for HSI Display)",
               "NAV - steerpoint, ILS/NAV - runway, TCN - tanker/airbase w/TACAN, ILS/TCN - airbase w/manual TACAN"));

         mPositions[20].addTipArea(
            new TipArea(874,165,900,177,
               "Range Indicator",
               "range in nm to selected TACAN, or steerpoint; Red flag indicates invalid data"));

         mPositions[20].addTipArea(
            new TipArea(970,163,998,176,
               "Course Indicator",
               "Displays the course dialed in via the Course Set Knob"));

         mPositions[20].addTipArea(
            new TipArea(939,235,40,
               "Horizontal Situation Indicator (HSI) Compass Card",
               "Displays relative position/orientation to a TACAN, steerpoint, runway location, tanker, or markpoint."));

         mPositions[20].addTipArea(
            new TipArea(883,271,12,
               "Heading Set Knob",
               "Rotates the heading marker in the HSI to the desired location."));

         mPositions[20].addTipArea(
            new TipArea(999,274,13,
               "Course Set Knob",
               "Use to dial in the desired course for the HSI."));

         mPositions[20].addTipArea(
            new TipArea(820,423,1013,516,
               "Ejection Lever",
               "Click and hold for several seconds to eject."));

         mPositions[20].addTipArea(
            new TipArea(474,196,752,610,
               "Left Kneeboard",
               "Provides flight and steerpoint info, briefing, and a moving map with the flight plan overlaid"));

         mPositions[20].addTipArea(
            new TipArea(281,148,334,199,
               "Emergency Jettison",
               "Jettison all A-G stores.  Should only be done in an emergency."));

         mPositions[20].addTipArea(
            new TipArea(340,123,409,200,
               "Landing Gear Lights",
               "Lit when gear is down, off when gear is up or damaged."));

         mPositions[20].addTipArea(
            new TipArea(417,96,458,165,
               "Tailhook Release",
               "Used for landing in emergency situations"));

         mPositions[20].addTipArea(
            new TipArea(394,219,450,329,
               "Landing Gear Handle",
               "Use to retract/lower the landing gear"));

         mPositions[20].addTipArea(
            new TipArea(326,245,16,
               "Parking Brake",
               "Use to toggle parking brake on/off"));

         mPositions[20].addTipArea(
            new TipArea(242,246,15,
               "Gnd Jett Enable",
               "Allows stores to be jettisoned on the ground"));

         mPositions[20].addTipArea(
            new TipArea(333,289,369,334,
               "Landing Lights toggle",
               ""));

         mPositions[20].addTipArea(
            new TipArea(293,318,20,
               "Horn Silencer",
               "Turns off the low speed warning tone"));

         mPositions[20].addTipArea(
            new TipArea(226,321,19,
               "Stores Config (Cat I or III)",
               "FCS limits aircraft based on this (I - light, III - heavy)"));

         poly = new Polygon();
         poly.addPoint(202,327);
         poly.addPoint(88,504);
         poly.addPoint(65,484);
         poly.addPoint(186,284);
         poly.addPoint(186,284);
         mPositions[20].addTipArea(
            new TipArea(poly,
               "Canopy Open Handle",
               ""));

         mPositions[20].addTipArea(
            new TipArea(195,364,221,421,
               "Speed Brake",
               "Status of air brakes."));

         mPositions[20].addTipArea(
            new TipArea(257,391,18,
               "RWR",
               "Controls whether or not the EWS system receives RWR data (launches)"));

         mPositions[20].addTipArea(
            new TipArea(258,431,16,
               "JMR",
               "Power switch for the electronic Jammer"));

         mPositions[20].addTipArea(
            new TipArea(353,409,18,
               "CH (Chaff)",
               "Power switch for the chaff dispenser"));

         mPositions[20].addTipArea(
            new TipArea(385,409,18,
               "FL (Flares)",
               "Power switch for the flares dispenser"));

         poly = new Polygon();
         poly.addPoint(5,642);
         poly.addPoint(43,638);
         poly.addPoint(63,663);
         poly.addPoint(6,687);
         poly.addPoint(6,687);
         mPositions[20].addTipArea(
            new TipArea(poly,
               "Idle Detent",
               "Used during ramp start to get the engine spinning up"));

         mPositions[20].addTipArea(
            new TipArea(340,478,19,
               "PRGM (Counter-measure Program)",
               "Selects program to execute in SEMI or AUTO mode",
               mFigures[mFigureEWSPrgm],10,50));

         mPositions[20].addTipArea(
            new TipArea(387,477,22,
               "MODE (EWS Main Mode)",
               "Off, STBY - manual reprogram, MAN - manual, SEMI/AUTO - automated behavior"));

         mPositions[20].addTipArea(
            new TipArea(154,465,183,491,
               "SEARCH",
               "Enabled - Search Radars will display on RWR, Disabled - Will flash when search radars detected"));

         mPositions[20].addTipArea(
            new TipArea(189,466,217,493,
               "ACTIVITY",
               "When lit, radar activity has been detected"));

         mPositions[20].addTipArea(
            new TipArea(155,499,185,525,
               "LOW ALTITUDE",
               "Enabled - priority for low-altitude threats (AAA, low-alt SAMs), Disabled - priority for high alt threats"));

         mPositions[20].addTipArea(
            new TipArea(188,500,213,525,
               "POWER",
               "Controls power to the RWR system"));

         poly = new Polygon();
         poly.addPoint(50,636);
         poly.addPoint(77,575);
         poly.addPoint(151,595);
         poly.addPoint(115,657);
         poly.addPoint(115,657);
         mPositions[20].addTipArea(
            new TipArea(poly,
               "Manual Pitch Override",
               "OVRD used for recovering from deep stall conditions"));

         poly = new Polygon();
         poly.addPoint(94,571);
         poly.addPoint(72,559);
         poly.addPoint(127,480);
         poly.addPoint(144,496);
         poly.addPoint(144,496);
         mPositions[20].addTipArea(
            new TipArea(poly,
               "Alt Gear Release",
               "Use to retract/lower the landing gear if the main handle does not work"));

         poly = new Polygon();
         poly.addPoint(393,690);
         poly.addPoint(427,696);
         poly.addPoint(452,636);
         poly.addPoint(452,344);
         poly.addPoint(437,341);
         poly.addPoint(437,524);
         poly.addPoint(386,665);
         poly.addPoint(386,665);
         mPositions[20].addTipArea(
            new TipArea(poly,
               "Ejection Controls Armed",
               "Used to arm the ejection seat"));

         mPositions[20].addTipArea(
            new TipArea(933,351,50,
               "Cockpit Air Vent",
               "Twist to open for air flow into the cockpit; Adjust for desired comfort level."));

         mPositions[20].addTipArea(
            new TipArea(228,361,436,499,
               "Countermeasures/EWS Panel",
               "Power switches for RWR, Jammer, and Missile Warning System (MWS); Chaff/Flare release programming"));

         //************************************************************************
         // tip areas (POSITION 21)
         //************************************************************************

         mPositions[21].addTipArea(
            new TipArea(832,306,25,
               "Jet Fuel Starter (JFS)",
               "Hydraulic start engine used to bootstrap the main engine during a ramp start"));

         mPositions[21].addTipArea(
            new TipArea(678,251,783,320,
               "Engine Controls",
               "Select primary or secondary engine control systems; secondary are not very forgiving"));

         mPositions[21].addTipArea(
            new TipArea(626,279,17,
               "AB RESET",
               "Used to reset the afterburner in case of problems"));

         mPositions[21].addTipArea(
            new TipArea(336,116,484,213,
               "Emergency Power Unit (EPU)",
               "NORM - automatically used if needed, OFF - never used, ON - used if engine below 80% RPM"));

         mPositions[21].addTipArea(
            new TipArea(495,266,25,
               "Electric Power Switch",
               "Controls source of eletric power; Off, Battery - use battery, Main - use main generator (use this if engine is running)"));

         poly = new Polygon();
         poly.addPoint(334,264);
         poly.addPoint(383,262);
         poly.addPoint(389,311);
         poly.addPoint(516,308);
         poly.addPoint(517,351);
         poly.addPoint(333,348);
         poly.addPoint(333,348);
         mPositions[21].addTipArea(
            new TipArea(poly,
               "Electrical System Warning Lights",
               ""));

         mPositions[21].addTipArea(
            new TipArea(338,401,440,490,
               "AVTR (Audiovisual Tape Recorder)",
               "Records activity through the HUD gun camera"));

         mPositions[21].addTipArea(
            new TipArea(338,496,436,542,
               "ACMI Recording Mode",
               "OFF, AUTO - records 30 secs after pickle or trigger, ON - continuous recording"));

         mPositions[21].addTipArea(
            new TipArea(366,589,40,
               "AVTR (Audiovisual Tape Recorder) Display Select",
               "Controls what is being recorded in the AVTR"));

         mPositions[21].addTipArea(
            new TipArea(449,385,578,658,
               "ECM",
               "Electronic Counter-measures control panel"));

         mPositions[21].addTipArea(
            new TipArea(250,178,286,215,
               "Air Refuel",
               "Opens/closes air refueling door; While open FLCS switches to landing gains mode"));

         mPositions[21].addTipArea(
            new TipArea(206,202,28,
               "Engine Feed (Pumping of fuel to engine)",
               "Off - no pumping (neg Gs will starve eng), NORM - all pumps on, AFT - aft only, FWD - fwd only"));

         mPositions[21].addTipArea(
            new TipArea(141,196,15,
               "Tank Inerting",
               "Replaces vapors in fuel tanks with nitrogen enriched air, reducing chance of explosion from flak"));

         mPositions[21].addTipArea(
            new TipArea(48,151,111,202,
               "Master Fuel",
               "Master on/off switch for the fuel supply."));

         mPositions[21].addTipArea(
            new TipArea(263,290,33,
               "CNI Control (Communications, Navigation, IFF)",
               "UFC - upfront controls (ICP) used, BACKUP - uses the TACAN selector below this control"));

         mPositions[21].addTipArea(
            new TipArea(257,344,310,407,
               "TACAN Function Knob",
               "T/R - airbase, A/ATR - tanker"));

         mPositions[21].addTipArea(
            new TipArea(29,426,319,589,
               "Exterior Lighting Panel",
               "Controls exterior lights"));

         mPositions[21].addTipArea(
            new TipArea(141,346,247,418,
               "TACAN channel selector",
               "used to select TACAN channel when CNI is in BACKUP mode"));

         poly = new Polygon();
         poly.addPoint(36,243);
         poly.addPoint(193,246);
         poly.addPoint(193,315);
         poly.addPoint(131,320);
         poly.addPoint(130,418);
         poly.addPoint(40,417);
         poly.addPoint(40,417);
         mPositions[21].addTipArea(
            new TipArea(poly,
               "IFF (Identify Friend or Foe) Controls",
               "Not used in the simulation"));

         mPositions[21].addTipArea(
            new TipArea(693,607,23,
               "Threat Volume Control",
               ""));

         mPositions[21].addTipArea(
            new TipArea(768,604,27,
               "MSL Volume Control",
               ""));

         mPositions[21].addTipArea(
            new TipArea(769,402,29,
               "COMM1 Volume Control",
               ""));

         mPositions[21].addTipArea(
            new TipArea(769,478,29,
               "COMM2 Volume Control",
               ""));

         mPositions[21].addTipArea(
            new TipArea(861,423,39,
               "UHF Radio",
               "Off, Main, Both, ADF"));

         //************************************************************************
         // tip areas (POSITION 22)
         //************************************************************************

         // pit position 22 (one left from 21) contains some of the same tip areas we already added
         // just shifted.  so, we clone the tip areas from position 21 to position 22 at an offset
         offsetX = 551;
         offsetY = 17;
         clones = mPositions[21].cloneTipAreas(offsetX,offsetY);
         for (int i = 0; i < clones.size(); i++)
         {
            mPositions[22].addTipArea((TipArea)clones.get(i));
         }

         mPositions[22].addTipArea(
            new TipArea(479,180,21,
               "Alt Flaps Extend",
               "Manually extend the trailing edge flaps; These are normally not deployed by the FLCS"));

         mPositions[22].addTipArea(
            new TipArea(352,311,28,
               "LE Flaps",
               "Control of leading edge flaps; Normally controlled by FLCS; if damaged, use this to lock them in place"));

         poly = new Polygon();
         poly.addPoint(278,369);
         poly.addPoint(436,368);
         poly.addPoint(438,449);
         poly.addPoint(569,446);
         poly.addPoint(568,570);
         poly.addPoint(278,571);
         poly.addPoint(278,571);
         mPositions[22].addTipArea(
            new TipArea(poly,
               "Manual Trim Control",
               "Manual flight control trimming when TRIM/AP DISC switch is in DISC position."));

         mPositions[22].addTipArea(
            new TipArea(449,364,546,444,
               "TRIM/AP DISC",
               "NORM - use flight stick hat switch to trim; DISC - use this panel to trim"));

         mPositions[22].addTipArea(
            new TipArea(239,188,20,
               "Mal & Ind Lights",
               "Pressing this causes all warning lights to come on."));

         mPositions[22].addTipArea(
            new TipArea(149,266,17,
               "Probe Heat",
               "This supplies heat to the external pressure probe, and so stops it freezing up."));

         //************************************************************************
         // fonts and sound effects
         //************************************************************************

         mPosNameFont = new Font(mPosNameFontName,mPosNameFontStyle,mPosNameFontSizePts);
         mTipFont = new Font(mTipFontName,mTipFontStyle,mTipFontSizePts);
         mSubTipFont = new Font(mSubTipFontName,mSubTipFontStyle,mSubTipFontSizePts);

         mSoundEffects = new SoundEffect[mNumSoundEffects];
         mSoundEffects[mSoundChangeView] = new SoundEffect(mSoundChangeViewFile);
         mSoundEffects[mSoundICPClick] = new SoundEffect(mSoundICPClickFile);
      }

      catch (Exception ex)
      {
         log("initialization exception: " + ex);
      }
   }

   public static void main(String[] args)
   {
      try
      {
         // check args
         boolean devMode = false;
         for (int x = 0; x < args.length; x++)
         {
            if (args[x].compareToIgnoreCase(mDevModeArg) == 0)
            {
               devMode = true;
            }
         }

         // initialize GUI and trainer objects
         PitTrainer t = new PitTrainer();
         t.mDevMode = devMode;
         t.init();

         // start the loading thread
         (new Thread(t)).start();
      }

      catch (Exception ex)
      {
         log("unhandled exception: " + ex);
      }
   }

   /**
    * worker thread which loads the needed resources and then when finished
    * updates the GUI for normal display.  updates the progress bar as
    * loading progresses.
    */
   public void run()
   {
      int x = 0;

      int totalResources = mNumPositions + mNumSoundEffects + mNumFigures;
      String baseText = mLoadProgress.getString() + " (";

      try
      {
         try
         {
            // load cockpit images
            for (x = 0; x < mNumPositions; x++)
            {
               mPositions[x].load();
               mLoadProgress.setValue(x+1);
               mLoadProgress.setString(baseText + Integer.toString(x+1) + " of " + Integer.toString(totalResources) + ")");
            }
         }

         catch (Exception e)
         {
            log("error loading cockpit image: " + mPositions[x]);
            throw e;
         }

         try
         {
            // load sound effects
            for (x = 0; x < mNumSoundEffects; x++)
            {
               mSoundEffects[x].load();
               mLoadProgress.setValue(mNumPositions + x + 1);
               mLoadProgress.setString(baseText + Integer.toString(mNumPositions+x+1) + " of " + Integer.toString(totalResources) + ")");
            }
         }

         catch (Exception e)
         {
            log("error loading sound effect: " + mSoundEffects[x]);
            throw e;
         }

         try
         {
            // load figures
            for (x = 0; x < mNumFigures; x++)
            {
               mFigures[x].load();
               mLoadProgress.setValue(mNumPositions + mNumSoundEffects + x + 1);
               mLoadProgress.setString(baseText + Integer.toString(mNumPositions+mNumSoundEffects+x+1) + " of " + Integer.toString(totalResources) + ")");
            }
         }

         catch (Exception e)
         {
            log("error loading figure: " + mFigures[x]);
            throw e;
         }

         // update UI by removing progress bar and adding in
         // the pit display panel
         mMainFrame.getContentPane().remove(mLoadProgress);
         mPitPanel = new PitPanel();
         mMainFrame.getContentPane().add(mPitPanel);
         mMainFrame.pack();
         mMainFrame.setSize(mWidth,mHeight);
         mPitPanel.setSize(mWidth,mHeight);
         mPitPanelUpdater = new PitPanelUpdater();
         SwingUtilities.invokeLater(mPitPanelUpdater);
         mMainFrame.setVisible(false);
         mMainFrame.setVisible(true);
         mPitKeyListener = new PitKeyListener();
         mMainFrame.addKeyListener(mPitKeyListener);
         mPitMouseListener = new PitMouseListener();
         mPitPanel.addMouseMotionListener(mPitMouseListener);
         mPitPanel.addMouseListener(mPitMouseListener);
      }

      catch (Exception ex)
      {
         log("resource loader exception: " + ex);
      }
   }

   /**
    * listens for keyboard activity
    */
   private class PitKeyListener implements KeyListener
   {
      public void keyPressed(KeyEvent e)
      {
         try
         {
            String ks = e.getKeyText(e.getKeyCode());

            int newPosition = mCurrentPosition;

            // LEFT
            if (ks.compareTo(mKeypadLeft) == 0)
            {
               newPosition = mPositions[mCurrentPosition].getLeft();
            }
            else if (ks.compareTo(mKeyLeft) == 0)
            {
               newPosition = mPositions[mCurrentPosition].getLeft();
            }
            // RIGHT
            else if (ks.compareTo(mKeypadRight) == 0)
            {
               newPosition = mPositions[mCurrentPosition].getRight();
            }
            else if (ks.compareTo(mKeyRight) == 0)
            {
               newPosition = mPositions[mCurrentPosition].getRight();
            }
            // UP
            else if (ks.compareTo(mKeypadUp) == 0)
            {
               newPosition = mPositions[mCurrentPosition].getUp();
            }
            else if (ks.compareTo(mKeyUp) == 0)
            {
               newPosition = mPositions[mCurrentPosition].getUp();
            }
            // DOWN
            else if (ks.compareTo(mKeypadDown) == 0)
            {
               newPosition = mPositions[mCurrentPosition].getDown();
            }
            else if (ks.compareTo(mKeyDown) == 0)
            {
               newPosition = mPositions[mCurrentPosition].getDown();
            }
            // SPACE (in dev mode, dumps the current polygon
            else if (ks.compareTo(mKeySpace) == 0)
            {
               if ((mDevMode) &&
                   (mPoly != null))
               {
                  System.out.println("poly = new Polygon();");

                  PathIterator pi = mPoly.getPathIterator(new AffineTransform());
                  double [] polyData = new double[6];
                  while (!pi.isDone())
                  {
                     pi.currentSegment(polyData);

                     System.out.println("poly.addPoint(" + Integer.toString((int)polyData[0]) + "," + Integer.toString((int)polyData[1]) + ");");

                     pi.next();
                  }

                  System.out.println("mPositions[].addTipArea(\n   new TipArea(poly,\n      \"\",\n      \"\"));");

                  mPoly = null;
               }
            }

            // if a position change has ocurred
            if (newPosition != mCurrentPosition)
            {
               // move to the new position
               mCurrentPosition = newPosition;

               // clear out any currently displayed tip area
               mCurrentTipArea = null;

               // trigger display update
               SwingUtilities.invokeLater(mPitPanelUpdater);

               // play view change sound effect
               mSoundEffects[mSoundChangeView].play();
            }
         }

         catch (Exception ex)
         {
            log("key handler exception: " + ex);
         }
      }

      public void keyReleased(KeyEvent e)
      {
      }

      public void keyTyped(KeyEvent e)
      {
      }
   }

   private boolean mMouseDragging = false;
   private MouseEvent mMouseDragPoint = null;
   private boolean mMMouseDragging = false;
   private MouseEvent mMMouseDragPoint = null;
   private Polygon mPoly = null;

   /**
    * listens for mouse activity
    */
   private class PitMouseListener implements MouseMotionListener, MouseListener
   {
      public void mouseDragged(MouseEvent e)
      {
      }

      public void mouseMoved(MouseEvent e)
      {
         try
         {
            boolean doUpdate = false;

            // see if the mouse is over a tip area
            TipArea newTipArea = mPositions[mCurrentPosition].findTipArea(e.getX(),e.getY());

            // if the current tip area value should change (moving to a different tip,
            // moving away from a tip, moving to a tip initially)
            if (newTipArea != mCurrentTipArea)
            {
               // set the new tip area value
               mCurrentTipArea = newTipArea;

               // trigger a display update
               SwingUtilities.invokeLater(mPitPanelUpdater);
            }

            // if close to the edge, display movement cursor
            Cursor newCursor = mCursorDefault;
            if (e.getX() < mMouseMoveDistX)
            {
               if (mPositions[mCurrentPosition].getLeft() != mCurrentPosition)
               {
                  newCursor = mCursorMoveLeft;
               }
            }
            else if (e.getX() > (mMainFrame.getWidth() - mMouseMoveDistX))
            {
               if (mPositions[mCurrentPosition].getRight() != mCurrentPosition)
               {
                  newCursor = mCursorMoveRight;
               }
            }
            else if (e.getY() < mMouseMoveDistY)
            {
               if (mPositions[mCurrentPosition].getUp() != mCurrentPosition)
               {
                  newCursor = mCursorMoveUp;
               }
            }
            else if (e.getY() > (mMainFrame.getHeight() - mMouseMoveDistY))
            {
               if (mPositions[mCurrentPosition].getDown() != mCurrentPosition)
               {
                  newCursor = mCursorMoveDown;
               }
            }
            else
            {
               if (newTipArea != null)
               {
                  if (newTipArea.getEvent() != null)
                  {
                     newCursor = mCursorClickable;
                  }
                  else
                  {
                     newCursor = mCursorNotClickable;
                  }
               }
               else
               {
                  newCursor = mCursorNotClickable;
               }
            }

            if (mMainFrame.getCursor() != newCursor)
            {
               mMainFrame.setCursor(newCursor);
            }
         }

         catch (Exception ex)
         {
            log("mouse handler exception: " + ex);
         }
      }

      public void mouseClicked(MouseEvent e)
      {
         if ((mDevMode) &&
             (e.getButton() == MouseEvent.BUTTON3))
         {
            if (mPoly == null)
            {
               mPoly = new Polygon();
            }

            mPoly.addPoint(e.getX(),e.getY());
         }
         else if (e.getButton() == MouseEvent.BUTTON1)
         {
            // if close to the edge, move view
            int newPosition = mCurrentPosition;
            if (e.getX() < mMouseMoveDistX)
            {
               newPosition = mPositions[mCurrentPosition].getLeft();
            }
            else if (e.getX() > (mMainFrame.getWidth() - mMouseMoveDistX))
            {
               newPosition = mPositions[mCurrentPosition].getRight();
            }
            else if (e.getY() < mMouseMoveDistY)
            {
               newPosition = mPositions[mCurrentPosition].getUp();
            }
            else if (e.getY() > (mMainFrame.getHeight() - mMouseMoveDistY))
            {
               newPosition = mPositions[mCurrentPosition].getDown();
            }

            boolean redraw = false;

            // if a clickable tip was clicked, allow cockpit positions to
            // handle the resulting event
            TipArea newTipArea = mPositions[mCurrentPosition].findTipArea(e.getX(),e.getY());
            if (newTipArea != null)
            {
               if (newTipArea.getEvent() != null)
               {
                  for (int j = 0; j < mNumPositions; j++)
                  {
                     mPositions[j].handleEvent(newTipArea.getEvent());
                  }

                  mSoundEffects[mSoundICPClick].play();

                  redraw = true;
               }
            }

            // if position has changed
            if (newPosition != mCurrentPosition)
            {
               // move to the new position
               mCurrentPosition = newPosition;

               // clear out any currently displayed tip area
               mCurrentTipArea = null;

               // play view change sound effect
               mSoundEffects[mSoundChangeView].play();

               redraw = true;
            }

            if (redraw)
            {
               // trigger display update
               SwingUtilities.invokeLater(mPitPanelUpdater);
            }
         }
      }

      public void mouseEntered(MouseEvent e)
      {
      }

      public void mouseExited(MouseEvent e)
      {
      }

      public void mousePressed(MouseEvent e)
      {
         if (mDevMode)
         {
            if (e.getButton() == MouseEvent.BUTTON1)
            {
               mMouseDragging = true;
               mMouseDragPoint = e;
            }
            else if (e.getButton() == MouseEvent.BUTTON2)
            {
               mMMouseDragging = true;
               mMMouseDragPoint = e;
            }
         }
      }

      public void mouseReleased(MouseEvent e)
      {
         if (mDevMode)
         {
            if (e.getButton() == MouseEvent.BUTTON1)
            {
               mMouseDragging = false;

               System.out.println(
                  "mPositions[].addTipArea(\n" +
                  "   new TipArea(" +
                     Integer.toString(mMouseDragPoint.getX()) + "," +
                     Integer.toString(mMouseDragPoint.getY()) + "," +
                     Integer.toString(e.getX()) + "," +
                     Integer.toString(e.getY()) + ",\n" +
                  "      \"\",\n" +
                  "      \"\"));\n");
            }
            else if (e.getButton() == MouseEvent.BUTTON2)
            {
               mMMouseDragging = false;

               int dx = e.getX() - mMMouseDragPoint.getX();
               int dy = e.getY() - mMMouseDragPoint.getY();
               int dist = (int)Math.sqrt((dx*dx)+(dy*dy));

               System.out.println(
                  "mPositions[].addTipArea(\n" +
                  "   new TipArea(" +
                     Integer.toString(mMMouseDragPoint.getX()) + "," +
                     Integer.toString(mMMouseDragPoint.getY()) + "," +
                     Integer.toString(dist) + ",\n" +
                  "      \"\",\n" +
                  "      \"\"));\n");
            }
         }
      }

   }

   /**
    * panel which displays the pit images and overlaid information
    */
   private class PitPanel extends JPanel
   {
      private BasicStroke stroke = new BasicStroke(2);

      public void paint(Graphics g)
      {
         try
         {
            // pit position
            mPositions[mCurrentPosition].render(g,getWidth(),getHeight());

            if (g instanceof Graphics2D)
            {
               Graphics2D g2D = (Graphics2D)g;
               g2D.setStroke(stroke);
            }

            // currently moused-over tip area
            if (mCurrentTipArea != null)
            {
               mCurrentTipArea.render(g);
            }
            // generic tip area
            else
            {
               mGenericTipArea.render(g);
            }
         }

         catch (Exception ex)
         {
            log("rendering exception: " + ex);
         }
      }
   }

   /**
    * used to trigger a display update
    */
   private class PitPanelUpdater implements Runnable
   {
      public void run()
      {
         Rectangle r = new Rectangle(mPitPanel.getWidth(),mPitPanel.getHeight());
         mPitPanel.repaint();
      }
   }
}
