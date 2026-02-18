package com.placement.student.ui;

import javax.swing.*;
import java.awt.*;

public class PolicyView extends JPanel {

    public PolicyView() {
        
        setLayout(new BorderLayout());
        setBackground(StudentTheme.BG);
add(StudentTheme.header("Policy", "Read placement rules and student responsibilities."), BorderLayout.NORTH);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(StudentTheme.fontRegular(13));
        area.setBackground(Color.WHITE);
        area.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        area.setText("1) Eligibility & Accuracy\n- Students must provide accurate academic details.\n- Any false information may lead to disqualification.\n\n2) Applications\n- Apply only if you meet eligibility criteria.\n- You can withdraw an application before it is processed.\n\n3) Interviews\n- Attend interviews on time.\n- For online interviews, use the meeting link and ensure stable internet.\n\n4) Offers\n- Offers must be accepted/rejected within the specified time.\n- Accepting an offer may lock you from applying to other roles (depending on policy).\n\n5) Off-campus Offers\n- Record off-campus opportunities and update their status.\n\n6) Conduct\n- Professional communication is required at all times.\n- Repeated no-shows may lead to suspension of placement privileges.\n\n(Edit this page to match your university policy.)\n");

        JScrollPane sp = new JScrollPane(area);
        JPanel center = StudentTheme.contentPanel();
        center.setLayout(new BorderLayout());
        center.add(sp, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        actions.setBackground(StudentTheme.BG);
        JButton close = new JButton("Close");
        StudentTheme.styleSecondaryButton(close);
        close.addActionListener(e -> StudentNav.goHome(this));
        actions.add(close);
        add(actions, BorderLayout.SOUTH);

    }
}
