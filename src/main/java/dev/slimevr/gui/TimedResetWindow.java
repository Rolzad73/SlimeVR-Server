package dev.slimevr.gui;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;

import java.awt.Container;
import java.awt.event.MouseEvent;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.embed.swing.JFXPanel;

import io.eiren.util.ann.AWTThread;

import dev.slimevr.VRServer;
import dev.slimevr.gui.swing.EJBox;

public class TimedResetWindow extends JFrame implements WindowListener {

	private final transient VRServer server;

	private static final int VOL_MIN = 0;
	private static final int VOL_MAX = 100;
	private static final int VOL_INIT = 100;

	private JRadioButton rdbFull;
	private JRadioButton rdbYaw;
	private JTextField resetTimeField;
	private JTextField resetDelayField;
	private JTextField soundFileField;
	private static int volume = 100;
	private JSlider volumeSlider;
	private static String soundFilePath = "";

	public TimedResetWindow(VRServer server) {
		super("Timed Reset Configuration");

		this.server = server;

		final JFXPanel fxPanel = new JFXPanel(); // just need to do this to get the JFX toolkit to initialize
		addWindowListener(this);

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		build();
	}

	@AWTThread
	private void build() {
		Container pane = getContentPane();

		pane.add(new EJBox(BoxLayout.PAGE_AXIS) {{
			add(new EJBox(BoxLayout.LINE_AXIS) {{
				setBorder(new EmptyBorder(i(5)));
				add(new JLabel("Reset type: "));
				rdbFull = new JRadioButton("Full");
				rdbYaw = new JRadioButton("Yaw");
				ButtonGroup bgResetType = new ButtonGroup();
				bgResetType.add(rdbFull);
				bgResetType.add(rdbYaw);
				add(rdbFull);
				add(rdbYaw);
				rdbFull.setSelected(server.config.getBoolean("FullReset", true));
				rdbYaw.setSelected(!rdbFull.isSelected());
			}});

			add(new EJBox(BoxLayout.LINE_AXIS) {{
				setBorder(new EmptyBorder(i(5)));
				add(new JLabel("Reset time (seconds) "));
				add(resetTimeField = new JTextField(String.valueOf(server.config.getInt("resetTime", 300)), 4));
			}});

			add(new EJBox(BoxLayout.LINE_AXIS) {{
				setBorder(new EmptyBorder(i(5)));
				add(new JLabel("Reset delay (seconds) "));
				add(resetDelayField = new JTextField(String.valueOf(server.config.getInt("resetDelay", 3)), 4));
			}});

			add(new EJBox(BoxLayout.LINE_AXIS) {{
				setBorder(new EmptyBorder(i(5)));
				add(new JLabel("Sound File: "));
				soundFilePath = server.config.getString("soundFile", "");
				add(new JButton("Choose File") {{
					addMouseListener(new MouseInputAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							JFileChooser fc = new JFileChooser();
							File currentFile = new File(soundFilePath);
							fc.setCurrentDirectory(currentFile);

							int returnVal = fc.showOpenDialog(null);
							if (returnVal == JFileChooser.APPROVE_OPTION) {
								File selectedFile = fc.getSelectedFile();
								soundFilePath = selectedFile.getAbsolutePath();
								soundFileField.setText(soundFilePath);
							}
						}
					});
				}});
			}});

			add(new EJBox(BoxLayout.LINE_AXIS) {{
				setBorder(new EmptyBorder(i(5)));
				add(soundFileField = new JTextField(soundFilePath));
				soundFileField.setEnabled(false);
			}});

			add(new EJBox(BoxLayout.LINE_AXIS) {{
				setBorder(new EmptyBorder(i(5)));
				add(new JLabel("Volume "));
				add(volumeSlider = new JSlider(JSlider.HORIZONTAL, VOL_MIN, VOL_MAX, VOL_INIT));
				volumeSlider.setMajorTickSpacing(10);
				volumeSlider.setPaintTicks(true);
				volume = server.config.getInt("soundVolume", 100);
				volumeSlider.setValue(volume);
				volumeSlider.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						if (!volumeSlider.getValueIsAdjusting()){
							volume = ((JSlider)e.getSource()).getValue();
						}
					}
				});
			}});

			add(new EJBox(BoxLayout.LINE_AXIS) {{
				setBorder(new EmptyBorder(i(5)));
				add(new JButton("Test") {{
					addMouseListener(new MouseInputAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							playSound(soundFilePath);
						}
					});
				}});
			}});

		}});

		// Pack and display
		pack();
		setLocationRelativeTo(null);
		setVisible(false);
	}

	private static synchronized void playSound(final String soundFilePath) {
		new Thread(new Runnable() {
			public void run() {
				try {
					// the music stops suddenly, but short audio is ok
					MediaPlayer mediaPlayer = new MediaPlayer(new Media(new File(soundFilePath).toURI().toString()));
					mediaPlayer.setVolume(volume/100f);
					mediaPlayer.play();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {
		saveSettings();
	}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@AWTThread
	private void saveSettings() {
		server.config.setProperty("FullReset", rdbFull.isSelected());
		server.config.setProperty("resetTime", Integer.valueOf(resetTimeField.getText()));
		server.config.setProperty("resetDelay", Integer.valueOf(resetDelayField.getText()));
		server.config.setProperty("soundFile", soundFilePath);
		server.config.setProperty("soundVolume", volume);
		server.saveConfig();
	}
}
