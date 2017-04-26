package main;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.SwingConstants;

import shared.Enumerations;
import shared.Enumerations.*;
import headset.HeadsetController;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;

public class UI extends JFrame {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtDirection;
	private JButton btnForward, btnLeft, btnRight, btnBackward, btnStop;
	private JProgressBar progressBar_Left;
	private JProgressBar progressBar_Forward;
	private JProgressBar progressBar_Right;
	private JProgressBar progressBar_Back;
	private int value;
	MotorState previousCommand = MotorState.stop;
	private JButton btnMotorControllerConnection;
	private JToggleButton tglbtnTrainingMode;
	private JButton btnHeadsetConnectionLevel;


	public UI() {
		setName("UI");
		getContentPane().setLayout(new BorderLayout(0, 4));

		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screensize.getWidth(), height = screensize.getHeight();
		double borderWidth = (width - 400) / 2, borderHeight = (height - 350) / 2;

		panel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode(borderWidth+"px"),
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("120px"),
				ColumnSpec.decode("22px"),
				ColumnSpec.decode("center:120px"),
				ColumnSpec.decode("20px"),
				ColumnSpec.decode("120px"),
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode(borderWidth+"px"),},
			new RowSpec[] {
				RowSpec.decode(borderHeight+"px"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("60px"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				RowSpec.decode("60px"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("60px"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.UNRELATED_GAP_ROWSPEC,
				RowSpec.decode("16px"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("40px"),
				RowSpec.decode(borderHeight+"px"),}));
		
				btnForward = new JButton("Forward");
				btnForward.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(tglbtnTrainingMode.isSelected()) {
							HeadsetController.option = MotorState.forward;
							HeadsetController.pending = 1;
						}
						else {
							value = 0;
							clearProgressBars();
							AppController.getInstance().sendState(MotorState.forward);
						}
					}
				});
				
				btnHeadsetConnectionLevel = new JButton("Headset Connection Level");
				btnHeadsetConnectionLevel.setBackground(Color.RED);
				btnHeadsetConnectionLevel.setOpaque(true);
				btnHeadsetConnectionLevel.setBorderPainted(false);
				panel.add(btnHeadsetConnectionLevel, "3, 3");
				btnHeadsetConnectionLevel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
					}
				});
				btnForward.setOpaque(true);
				panel.add(btnForward, "5, 3, fill, fill");
		
				btnMotorControllerConnection = new JButton("Motor Controller Connection");
				btnMotorControllerConnection.setBackground(Color.red);
				btnMotorControllerConnection.setOpaque(true);
				btnMotorControllerConnection.setBorderPainted(false);
				btnMotorControllerConnection.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
					}
				});
				panel.add(btnMotorControllerConnection, "7, 3");
		
				progressBar_Forward = new JProgressBar();
				panel.add(progressBar_Forward, "5, 5");
		
				btnLeft = new JButton("Left");
				btnLeft.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(tglbtnTrainingMode.isSelected()) {
							HeadsetController.option = MotorState.left;
							HeadsetController.pending = 1;
						}
						else {
							value = 0;
							clearProgressBars();
							AppController.getInstance().sendState(MotorState.left);
						}
					}
				});
				btnLeft.setOpaque(true);
				panel.add(btnLeft, "3, 6, fill, fill");
				
						btnRight = new JButton("Right");
						btnRight.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								if(tglbtnTrainingMode.isSelected()) {
									HeadsetController.option = MotorState.right;
									HeadsetController.pending = 1;
								}
								else {
									value = 0;
									clearProgressBars();
									AppController.getInstance().sendState(MotorState.right);
								}
							}
						});
						btnRight.setOpaque(true);
						panel.add(btnRight, "7, 6, fill, fill");
		
				progressBar_Left = new JProgressBar();
				panel.add(progressBar_Left, "3, 8");
		
				btnBackward = new JButton("Backward");
				btnBackward.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(tglbtnTrainingMode.isSelected()) {
							HeadsetController.option = MotorState.backward;
							HeadsetController.pending = 1;
						}
						else {
							value = 0;
							clearProgressBars();
							AppController.getInstance().sendState(MotorState.backward);
						}
					}
				});
				
						progressBar_Right = new JProgressBar();
						panel.add(progressBar_Right, "7, 8");
				
						tglbtnTrainingMode = new JToggleButton("Training Mode");
						tglbtnTrainingMode.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								EnableDisableTraining(tglbtnTrainingMode.isSelected());
							}
						});
						panel.add(tglbtnTrainingMode, "3, 10");
				btnBackward.setOpaque(true);
				panel.add(btnBackward, "5, 10, fill, fill");

		btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(tglbtnTrainingMode.isSelected()) {
					HeadsetController.option = MotorState.stop;
					HeadsetController.pending = 1;
				}
				else {
					value = 0;
					clearProgressBars();
					AppController.getInstance().sendState(MotorState.stop);
				}
			}
		});
		panel.add(btnStop, "5, 6, fill, fill");
		
				progressBar_Back = new JProgressBar();
				panel.add(progressBar_Back, "5, 12");

		txtDirection = new JTextField();
		txtDirection.setHorizontalAlignment(SwingConstants.CENTER);
		txtDirection.setText("Stop");
		panel.add(txtDirection, "3, 16, 5, 1, fill, fill");
		txtDirection.setColumns(10);

		JLabel lblDirection = new JLabel("Direction");
		lblDirection.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblDirection, "5, 14, fill, top");

		//setSize(410,397);
		setSize((int)width,(int)height);
		setVisible(true);
	}
	synchronized public void changeHeadsetConnectionColor(String color){
		if (color.equalsIgnoreCase("red")){
			//progressBar_MotorController.setForeground(Color.RED);
			btnHeadsetConnectionLevel.setBackground(Color.red);
		}
		else if(color.equalsIgnoreCase("green")){
			//progressBar_MotorController.setForeground(Color.GREEN);
			System.out.println("changed to green");
			btnHeadsetConnectionLevel.setBackground(Color.green);

		}
	}
	synchronized public void changeMotorControllerConnectionColor(String color){
		if (color.equalsIgnoreCase("red")){
			btnMotorControllerConnection.setBackground(Color.red);
		}
		else if(color.equalsIgnoreCase("green")){
			btnMotorControllerConnection.setBackground(Color.green);

		}
	}


	synchronized public void postMessage(String string) {
		txtDirection.setText(string);
	}

	synchronized private void clearProgressBars(){
		progressBar_Back.setValue(0);
		progressBar_Forward.setValue(0);
		progressBar_Right.setValue(0);
		progressBar_Left.setValue(0);
	}

	synchronized private void updateProgressBars(MotorState selectedState, int valueUpdate){
		switch(selectedState) {
			case backward:
				progressBar_Back.setValue(valueUpdate);
				break;
			case forward:
				progressBar_Forward.setValue(valueUpdate);
				break;
			case right:
				progressBar_Right.setValue(valueUpdate);
				break;
			case left:
				progressBar_Left.setValue(valueUpdate);
				break;
			case not_found:
				break;
			default:
				progressBar_Back.setValue(valueUpdate);
				progressBar_Forward.setValue(valueUpdate);
				progressBar_Right.setValue(valueUpdate);
				progressBar_Left.setValue(valueUpdate);
				break;
			}

	}

	synchronized public void registerCommand(MotorState selectedState, int power){
		System.out.println("Command received: " + selectedState.toString() + " With power: " + power);
		if (selectedState != previousCommand && selectedState != MotorState.not_found) {
			value = 0;
			updateProgressBars(previousCommand, value);
			previousCommand = selectedState;
		}
		if(tglbtnTrainingMode.isSelected()) {
			value = 0;
			updateProgressBars(selectedState, power);
			return;
		}

		value = value + power;
		if(value < 500){
			updateProgressBars(selectedState, value);
		}
		else{
			System.out.println("Command sent: " + selectedState.toString());
			value = 0;
			updateProgressBars(selectedState, value);
			AppController.getInstance().sendState(selectedState);

		}
		return;
	}

	private void EnableDisableTraining(boolean trainingMode) {
		if(HeadsetController.connectionStatus == Enumerations.HeadsetConnectionState.Disconnected) {
			txtDirection.setText("Headset Not Connected");
			tglbtnTrainingMode.setSelected(false);
			return;
		}
		if(trainingMode) {
			txtDirection.setText("Training Mode ON");
			AppController.getInstance().sendState(MotorState.stop);
			HeadsetController.option = MotorState.training;
			HeadsetController.pending = 1;

			progressBar_Back.setValue((int) HeadsetController.getSkillLevel(Enumerations.motorStateToInt(MotorState.backward)));
			if(progressBar_Back.getValue() < 5) btnBackward.setBackground(Color.red);
			else if(progressBar_Back.getValue() < 50) btnBackward.setBackground(Color.yellow);
			else btnBackward.setBackground(Color.green);

			progressBar_Forward.setValue((int) HeadsetController.getSkillLevel(Enumerations.motorStateToInt(MotorState.forward)));
			if(progressBar_Forward.getValue() < 5) btnForward.setBackground(Color.red);
			else if(progressBar_Forward.getValue() < 50) btnForward.setBackground(Color.yellow);
			else btnForward.setBackground(Color.green);

			progressBar_Right.setValue((int) HeadsetController.getSkillLevel(Enumerations.motorStateToInt(MotorState.right)));
			if(progressBar_Right.getValue() < 5) btnRight.setBackground(Color.red);
			else if(progressBar_Right.getValue() < 50) btnRight.setBackground(Color.yellow);
			else btnRight.setBackground(Color.green);

			progressBar_Left.setValue((int) HeadsetController.getSkillLevel(Enumerations.motorStateToInt(MotorState.left)));
			if(progressBar_Left.getValue() < 5) btnLeft.setBackground(Color.red);
			else if(progressBar_Left.getValue() < 50) btnLeft.setBackground(Color.yellow);
			else btnLeft.setBackground(Color.green);
		}
		else {
			txtDirection.setText("Training Mode OFF");
			HeadsetController.SavingLoadingFunction(true);

			progressBar_Back.setValue(0);
			btnBackward.setBackground(Color.lightGray);

			progressBar_Forward.setValue(0);
			btnForward.setBackground(Color.lightGray);

			progressBar_Right.setValue(0);
			btnRight.setBackground(Color.lightGray);

			progressBar_Left.setValue(0);
			btnLeft.setBackground(Color.lightGray);
		}
	}

}
