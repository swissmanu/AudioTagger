package ch.hsr.audiotagger.ui.windows;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import ch.hsr.audiotagger.ui.GUIHelper;
import ch.hsr.audiotagger.ui.GUIImageManager;
import ch.hsr.audiotagger.ui.components.SpriteAnimationComponent;

/**
 * Führt ein {@link Runnable} im Hintergrund aus, währenddessen dem Benutzer
 * ein Dialog mit einer {@link JProgressBar} und einem kurzen Text präsentiert
 * wird.
 * 
 * @author Manuel Alabor
 */
public class WaitDialog extends JDialog {

	private final static JLabel lblStatus = new JLabel("");
	private final static SpriteAnimationComponent pgaAnimation = new SpriteAnimationComponent(
			GUIImageManager.getInstance().getImage("animation/process-working.png"),
			32, 32, 31);
	private Runnable workToDo = null;
	private WorkerThread worker = null;
	
	private static final long serialVersionUID = 3518608640897073890L;
	
	public WaitDialog(Window owner, String dialogTitle, String statusText, Runnable workToDo) {
		super(owner, dialogTitle);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setSize(320,73);
		GUIHelper.centerOnComponent(this, owner);
		setResizable(false);
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		setModal(true);
		
		addWindowListener(new DialogListener());
		
		lblStatus.setText(statusText);
		this.workToDo = workToDo;
		
		setContentPane(buildGui());
	}
	
	private JComponent buildGui() {
		JPanel gui = new JPanel(new BorderLayout());
		
		/* Vorbereiten: */
		lblStatus.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		gui.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		
		/* GUI zusammenstellen: */
		gui.add(lblStatus, BorderLayout.CENTER);
		gui.add(pgaAnimation, BorderLayout.WEST);
		
		return gui;
	}
	
	// Hilfsklassen & Listener -------------------------------------------------
	/**
	 * Dieser {@link WindowListener} startet sobald der Dialog angezeigt wird
	 * einen {@link WorkerThread}, welcher das übergebene {@link Runnable}
	 * <code>workToDo</code> ausgeführt wird.
	 * 
	 * @see WorkerThread
	 * @see WaitDialog#WaitDialog(JFrame, String, String, Runnable)
	 */
	private class DialogListener extends WindowAdapter {
	    public void windowOpened(WindowEvent we) {
	        worker = new WorkerThread(workToDo, WaitDialog.this);
	        worker.start();
	    }
    }

	private class WorkerThread extends Thread {
	    
	    private Runnable workToDo;
	    private Window toDispose;
	    
	    public WorkerThread(Runnable workToDo, Window toDispose) {
	        this.workToDo = workToDo;
	        this.toDispose = toDispose;
	    }
	    
	    public void run() {
	        workToDo.run();
	        
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    toDispose.dispose();
                }
            });
	    }
	    
	}
	
}
