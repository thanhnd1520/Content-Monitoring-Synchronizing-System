package client;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;

import java.awt.Color;
import java.awt.Font;
import java.awt.Component;
import java.awt.Cursor;

public class ClientView extends JFrame {

	private JPanel contentPane;
	private JTextField usernameTextField;
	private JTextField passwordTextField;
	private JLabel errorUsernameLable;
	private JLabel errorPasswordLabel;
	private JLabel connectStateLabel;
	
	private String usernameError;
	private String passwordError;
	private boolean checkLogin;
	private ClientHandler clientHandler;
	
	public void setClientHandler(ClientHandler clientHandler) {
		this.clientHandler = clientHandler;
	}
	
	public void setCheckLogin(boolean check) {
		this.checkLogin = check;
	}
	
	public boolean getCheckLogin() {
		return this.checkLogin;
	}
	public void run() {
		this.setVisible(true);
	}

	public void setFailLoginMessage() {
		String message = "tài khoản hoặc mật khẩu không chính xác";
		this.errorUsernameLable.setText(message);
		this.errorPasswordLabel.setText(message);
	}
	public void setFailUpdate() {
		String message = "chưa đăng nhập";
		this.errorUsernameLable.setText(message);
		this.errorPasswordLabel.setText(message);
	}

	/**
	 * Create the frame.
	 */
	public ClientView() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		usernameTextField = new JTextField();
		usernameTextField.setBounds(204, 74, 191, 20);
		contentPane.add(usernameTextField);
		usernameTextField.setColumns(10);
		
		passwordTextField = new JTextField();
		passwordTextField.setBounds(204, 119, 191, 20);
		contentPane.add(passwordTextField);
		passwordTextField.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Username: ");
		lblNewLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		lblNewLabel.setBorder(new LineBorder(new Color(0, 0, 0)));
		lblNewLabel.setBounds(74, 74, 100, 20);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Password");
		lblNewLabel_1.setAlignmentX(Component.RIGHT_ALIGNMENT);
		lblNewLabel_1.setBorder(new LineBorder(new Color(0, 0, 0)));
		lblNewLabel_1.setBounds(74, 120, 100, 18);
		contentPane.add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("LOGIN");
		lblNewLabel_2.setFont(new Font("Tw Cen MT Condensed", Font.BOLD, 30));
		lblNewLabel_2.setBounds(156, 11, 137, 52);
		contentPane.add(lblNewLabel_2);
		
		JButton btnNewButton = new JButton("Login");
		
		// login button
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(validateLogin()) {
					String username = usernameTextField.getText();
					String password = passwordTextField.getText();
					try {
						if(!checkLogin) {
							clientHandler.sendLogin(username, password);
						}
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		btnNewButton.setBackground(Color.GREEN);
		btnNewButton.setBounds(61, 163, 89, 23);
		contentPane.add(btnNewButton);
		
		errorUsernameLable = new JLabel("");
		errorUsernameLable.setBounds(204, 94, 191, 14);
		contentPane.add(errorUsernameLable);
		
		errorPasswordLabel = new JLabel("");
		errorPasswordLabel.setBounds(204, 137, 191, 15);
		contentPane.add(errorPasswordLabel);
		
		connectStateLabel = new JLabel("");
		connectStateLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		connectStateLabel.setForeground(Color.GREEN);
		connectStateLabel.setBounds(164, 221, 106, 20);
		contentPane.add(connectStateLabel);
		
		JButton updateButton = new JButton("update");
		updateButton.setBackground(Color.RED);
		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(checkLogin) {
					clientHandler.submit();
				}
				else {
					setFailUpdate();
				}
			}
		});
		updateButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		updateButton.setBounds(176, 163, 89, 23);
		contentPane.add(updateButton);
		
		JButton btnNewButton_1 = new JButton("Synchronize");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
//				if(checkLogin) {
//					clientHandler.synchronize();
//				}
//				else {
//					setFailUpdate();
//				}
			}
			
		});
		btnNewButton_1.setBounds(290, 163, 105, 23);
		contentPane.add(btnNewButton_1);
		
		init();
		run();
	}
	
	private void init() {
		usernameError = passwordError = "";
		this.checkLogin = false;
	}
	
	private boolean validateLogin() {
		String username = "";
		String password = "";
		username += usernameTextField.getText();
		password += passwordTextField.getText();
		if(username == "") {
			errorUsernameLable.setText("nhập tên người dùng");
		}
		else {
			errorUsernameLable.setText("");
		}
		if(password == "") {
			errorPasswordLabel.setText("nhập mật khẩu");
		}
		else {
			errorPasswordLabel.setText("");
		}
		if(username == "" || password == "") {
			return false;
		}
		else {
			return true;
		}
	}
	
	public void setConnectStringView(String connectString) {
		connectStateLabel.setText(connectString);
	}
}
