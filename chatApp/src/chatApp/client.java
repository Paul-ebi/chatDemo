package chatApp;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;



public class client extends JFrame{

	private static final long serialVersionUID = 1L;
	private JFrame frame;
	private JTextField clientTypingBoard;
	private JList clientActiveUsersList;
	private JTextArea clientMessageBoard;
	private JButton clientKillProcessBtn;
	private JRadioButton oneToNRadioBtn;
	private JRadioButton broadcastBtn;

	DataInputStream inputStream;
	DataOutputStream outStream;
	DefaultListModel<String> dm;
	String id, clientIds = "";

	public client(String id, Socket s) {
		initialize();

		this.id = id;
		try {
			frame.setTitle("Client :: " + id); // set title of UI
			dm = new DefaultListModel<String>(); // default list used for showing active users on UI
			clientActiveUsersList.setModel(dm);// show that list on UI component JList named clientActiveUsersList
			inputStream = new DataInputStream(s.getInputStream()); // initilize input and output stream
			outStream = new DataOutputStream(s.getOutputStream());
			new Read().start(); // create a new thread for reading the messages
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	class Read extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					String m = inputStream.readUTF();  // read message from server, this will contain :;.,/=<comma seperated clientsIds>
					System.out.println("inside read thread : " + m); // print message for testing purpose
					if (m.contains(":;.,/=")) { // prefix(i know its random)
						m = m.substring(6); // comma separated all active user ids
						dm.clear(); // clear the list before inserting fresh elements
						StringTokenizer st = new StringTokenizer(m, ","); // split all the clientIds and add to dm below
						while (st.hasMoreTokens()) {
							String u = st.nextToken();
							if (!id.equals(u)) // we do not need to show own user id in the active user list pane
								dm.addElement(u); // add all the active user ids to the defaultList to display on active
							// user pane on client view
						}
					} else {
						clientMessageBoard.append("" + m + "\n"); //otherwise print on the clients message board
					}
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}	
	private void initialize() {
		// TODO Auto-generated method stub
		frame = new JFrame();
		frame.setBounds(100, 100, 926, 645);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.getContentPane().setBackground(Color.GRAY);
		frame.setTitle("Client View");
		
		JPanel panel = new JPanel();
        frame.add(panel);
        


		clientMessageBoard = new JTextArea(530, 495);
		clientMessageBoard.setEditable(false);
		clientMessageBoard.setBounds(12, 25, 530, 495);
		clientMessageBoard.setFont((new Font("Tahoma", Font.PLAIN, 22)));
		clientMessageBoard.setBackground(Color.white);
		JScrollPane scroll = new JScrollPane(clientMessageBoard);
		scroll.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		clientMessageBoard.setLineWrap(true);
		clientMessageBoard.setWrapStyleWord(true);
		frame.getContentPane().add(scroll);
		frame.getContentPane().add(clientMessageBoard);
		panel.add(scroll);
		

		clientTypingBoard = new JTextField();
		clientTypingBoard.setHorizontalAlignment(SwingConstants.LEFT);
		clientTypingBoard.setBounds(12, 533, 530, 54);
		frame.getContentPane().add(clientTypingBoard);
		clientTypingBoard.setColumns(10);

		JButton clientSendMsgBtn = new JButton("Send");
		clientSendMsgBtn.setForeground(Color.white);
		clientSendMsgBtn.setBackground(Color.GREEN);
		clientSendMsgBtn.addActionListener(new ActionListener() { // action to be taken on send message button
			public void actionPerformed(ActionEvent e) {
				String textAreaMessage = clientTypingBoard.getText(); // get the message from textbox
				Date date = Calendar.getInstance().getTime();  
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");  

				if (textAreaMessage != null && !textAreaMessage.isEmpty()) {  // only if message is not empty then send it further otherwise do nothing
					try {
						String messageToBeSentToServer = "";
						String cast = "broadcast"; // this will be an identifier to identify type of message
						int flag = 0; // flag used to check whether used has selected any client or not for multicast 
						if (oneToNRadioBtn.isSelected()) { // if 1-to-N is selected then do this
							cast = "multicast"; 
							List<String> clientList = clientActiveUsersList.getSelectedValuesList(); // get all the users selected on UI
							if (clientList.size() == 0) // if no user is selected then set the flag for further use
								flag = 1;
							for (String selectedUsr : clientList) { // append all the usernames selected in a variable
								if (clientIds.isEmpty())
									clientIds += selectedUsr;
								else
									clientIds += "," + selectedUsr;
							}
							messageToBeSentToServer = cast + ":" + clientIds + ":" + textAreaMessage+ "....(" + new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(date)+")"+"\n\n"; // prepare message to be sent to server
						} else {
							messageToBeSentToServer = cast + ":" + textAreaMessage+ "....(" + new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(date)+")"+"\n\n"; // in case of broadcast we don't need to know userIds
						}
						if (cast.equalsIgnoreCase("multicast")) { 
							if (flag == 1) { // for multicast check if no user was selected then prompt a message dialog
								JOptionPane.showMessageDialog(frame, "No user selected");
							} else { // otherwise just send the message to the user
								outStream.writeUTF(messageToBeSentToServer);
								clientTypingBoard.setText("");
								clientMessageBoard.append("< You sent msg to " + clientIds + ">" + textAreaMessage + "....(" + new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(date)+")"+"\n\n"); //show the sent message to the sender's message board
							}
						} else { // in case of broadcast
							outStream.writeUTF(messageToBeSentToServer);
							clientTypingBoard.setText("");
							clientMessageBoard.append("< You sent msg to All >" + textAreaMessage +"....(" + new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(date)+")"+"\n\n");
						}
						clientIds = ""; // clear the all the client ids 
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(frame, "User is Offline."); // if user doesn't exist then show message
					}
					/*
					 * String log = clientMessageBoard.getText(); PrintWriter out; try { out = new
					 * PrintWriter("messageLog.txt"); out.println(log); } catch (Exception eq) { //
					 * TODO Auto-generated catch block eq.printStackTrace(); }
					 */
					String log = clientMessageBoard.getText();
					File f = new File("audits.txt");
					FileWriter fw;
					try {
						fw = new FileWriter(f);
						fw.write(log);
						fw.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				}
			}


		});

		clientSendMsgBtn.setBounds(554, 533, 137, 54);
		frame.getContentPane().add(clientSendMsgBtn);

		clientActiveUsersList = new JList();
		clientActiveUsersList.setToolTipText("Active Users");
		clientActiveUsersList.setBounds(554, 63, 327, 457);
		clientActiveUsersList.setBackground(Color.white);
		frame.getContentPane().add(clientActiveUsersList);

		clientKillProcessBtn = new JButton("Exit Chat");
		clientKillProcessBtn.setForeground(Color.white);
		clientKillProcessBtn.setBackground(Color.red);
		clientKillProcessBtn.addActionListener(new ActionListener() { // kill process event
			public void actionPerformed(ActionEvent e) {
				try {
					outStream.writeUTF("exit"); // closes the thread and show the message on server and client's message
					// board
					clientMessageBoard.append("You are disconnected now.\n");
					frame.dispose(); // close the frame 
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		clientKillProcessBtn.setBounds(703, 533, 180, 54);
		frame.getContentPane().add(clientKillProcessBtn);

		JLabel lblNewLabel = new JLabel("Active Users");
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblNewLabel.setBounds(559, 43, 95, 16);
		frame.getContentPane().add(lblNewLabel);

		oneToNRadioBtn = new JRadioButton("1 to N");
		oneToNRadioBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clientActiveUsersList.setEnabled(true);
			}
		});
		oneToNRadioBtn.setSelected(true);
		oneToNRadioBtn.setBounds(682, 24, 72, 25);
		frame.getContentPane().add(oneToNRadioBtn);

		broadcastBtn = new JRadioButton("Broadcast");
		broadcastBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clientActiveUsersList.setEnabled(false);
			}
		});
		broadcastBtn.setBounds(774, 24, 107, 25);
		frame.getContentPane().add(broadcastBtn);

		ButtonGroup btngrp = new ButtonGroup();
		btngrp.add(oneToNRadioBtn);
		btngrp.add(broadcastBtn);
		//frame.pack();
		frame.setVisible(true);
	}




}
