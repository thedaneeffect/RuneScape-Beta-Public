package rs;

import java.awt.*;

import javax.swing.JFrame;

public final class GameFrame extends JFrame {

	private static final long serialVersionUID = -2056000718285277925L;

	private GameShell shell;

	public GameFrame(GameShell shell, int w, int h) {
		this.shell = shell;
		setTitle("Jagex");
		setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		shell.setPreferredSize(new Dimension(w, h));
		add(shell);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		toFront();
		shell.requestFocus();
	}

	@Override
	public void dispose() {
		super.dispose();
		shell.destroy();
	}

	@Override
	public void update(Graphics g) {
		shell.update(g);
	}

	@Override
	public void paint(Graphics g) {
		shell.paint(g);
	}
}
