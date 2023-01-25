package chatApp;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class clientLogin extends JFrame {

	private JFrame frame;
	private JTextField clientUserName;
	private int port = 8818;

	public clientLogin() {
		// send constructor to initialize method

		initialize();
	}

	private void initialize() {
		// to construct the GUI of the class
		frame = new JFrame();
		frame.setBounds(100, 100, 619, 320);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("Register Username");
		frame.getContentPane().setBackground(Color.DARK_GRAY);


		clientUserName = new JTextField();
		clientUserName.setBounds(207, 50, 276, 45);
		clientUserName.setFont((new Font("Tahoma", Font.PLAIN, 24)));
		frame.getContentPane().add(clientUserName);
		clientUserName.setColumns(10);

		JButton clientLoginBtn = new JButton("Connect");
		clientLoginBtn.addActionListener(new ActionListener() { //action will be taken on clicking login button
			public void actionPerformed(ActionEvent e) {
				try {
					String id = clientUserName.getText(); // username entered by user
					Socket s = new Socket("localhost", port); // create a socket
					DataInputStream inputStream = new DataInputStream(s.getInputStream()); // create input and output stream
					DataOutputStream outStream = new DataOutputStream(s.getOutputStream());
					outStream.writeUTF(id); // send username to the output stream

					String msgFromServer = new DataInputStream(s.getInputStream()).readUTF(); // receive message on socket
					//if server sent this message then prompt user to enter other username
					if(msgFromServer.equals("Username already taken")) {
						JOptionPane.showMessageDialog(frame,  "Username already taken\n"); // show message in other dialog box
					}else {
						new client(id, s); // otherwise, create a new thread of Client view and close the register jframe
						frame.dispose();
					}
				}catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		clientLoginBtn.setFont(new Font("Tahoma", Font.PLAIN, 17));
		clientLoginBtn.setBounds(207, 139, 100, 30);
		clientLoginBtn.setBackground(Color.red);
		clientLoginBtn.setForeground(Color.white);
		frame.getContentPane().add(clientLoginBtn);

		JLabel lblNewLabel = new JLabel("Username");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 17));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(44, 55, 132, 47);
		lblNewLabel.setForeground(Color.YELLOW);
		frame.getContentPane().add(lblNewLabel);
	}


	public static void main(String[] args) { // main function which will make UI visible
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					clientLogin window = new clientLogin();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


}
