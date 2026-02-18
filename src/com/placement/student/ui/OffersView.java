package com.placement.student.ui;

import com.placement.student.controller.OfferController;
import com.placement.student.dao.OfferDAO;
import com.placement.student.dao.OfferDAOImpl;
import com.placement.student.model.Offer;
import com.placement.student.service.OfferService;
import com.placement.student.service.ServiceException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class OffersView extends JPanel {

    private final long studentId;
    private final OfferController offerController;

    private final DefaultTableModel model;
    private final JTable table;

    private final CardLayout card = new CardLayout();
    private final JPanel tableOrEmpty = new JPanel(card);

    public OffersView(long studentId) {
        
        setLayout(new BorderLayout());
        setBackground(StudentTheme.BG);
this.studentId = studentId;

        OfferDAO offerDAO = new OfferDAOImpl();
        OfferService offerService = new OfferService(offerDAO);
        this.offerController = new OfferController(offerService);

        model = new DefaultTableModel(new Object[]{"Offer ID", "Application ID", "Joining Date", "Status", "Issued At"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        StudentTheme.styleTable(table);
        table.getColumnModel().getColumn(3).setCellRenderer(StudentTheme.statusChipRenderer());

        add(StudentTheme.header("Offers", "Accept or reject offers."), BorderLayout.NORTH);

        JPanel center = StudentTheme.contentPanel();
        center.setLayout(new BorderLayout(12, 12));
        center.add(buildTopBar(), BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(table);
        tableOrEmpty.add(sp, "TABLE");
        tableOrEmpty.add(StudentTheme.emptyState("No offers yet", "When companies send offers, they will appear here."), "EMPTY");
        center.add(tableOrEmpty, BorderLayout.CENTER);
        center.add(buildActions(), BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        refreshOffers();
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(StudentTheme.BG);

        JLabel title = new JLabel("Offers for Student ID: " + studentId);
        title.setFont(StudentTheme.fontBold(13));
        title.setForeground(new Color(31, 41, 55));
        p.add(title, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh");
        StudentTheme.styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> refreshOffers());
        p.add(refreshBtn, BorderLayout.EAST);

        return p;
    }

    private JPanel buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        p.setBackground(StudentTheme.BG);

        JButton acceptBtn = new JButton("Accept");
        JButton rejectBtn = new JButton("Reject");
        JButton closeBtn = new JButton("Close");

        StudentTheme.stylePrimaryButton(acceptBtn);
        StudentTheme.styleSecondaryButton(rejectBtn);
        StudentTheme.styleSecondaryButton(closeBtn);

        acceptBtn.addActionListener(e -> acceptSelected());
        rejectBtn.addActionListener(e -> rejectSelected());
        closeBtn.addActionListener(e -> StudentNav.goHome(this));

        p.add(acceptBtn);
        p.add(rejectBtn);
        p.add(closeBtn);

        return p;
    }

    private void refreshOffers() {
        try {
            List<Offer> offers = offerController.viewOffers(studentId);
            model.setRowCount(0);
            for (Offer o : offers) {
                model.addRow(new Object[]{
                        o.getOfferId(),
                        o.getApplicationId(),
                        o.getJoiningDate(),
                        o.getStatus(),
                        o.getIssuedAt()
                });
            }
            if (model.getRowCount() == 0) card.show(tableOrEmpty, "EMPTY");
            else card.show(tableOrEmpty, "TABLE");
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private long getSelectedOfferId() {
        int row = table.getSelectedRow();
        if (row < 0) return -1;
        Object val = model.getValueAt(row, 0);
        return (val instanceof Number) ? ((Number) val).longValue() : Long.parseLong(val.toString());
    }

    private void acceptSelected() {
        long offerId = getSelectedOfferId();
        if (offerId <= 0) {
            UiUtil.error("Select an offer first.");
            return;
        }
        if (!UiUtil.confirm("Accept offer ID " + offerId + "?")) return;

        try {
            boolean ok = offerController.accept(studentId, offerId);
            UiUtil.info(ok ? "Offer accepted." : "Offer could not be accepted.");
            refreshOffers();
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void rejectSelected() {
        long offerId = getSelectedOfferId();
        if (offerId <= 0) {
            UiUtil.error("Select an offer first.");
            return;
        }
        if (!UiUtil.confirm("Reject offer ID " + offerId + "?")) return;

        try {
            boolean ok = offerController.reject(studentId, offerId);
            UiUtil.info(ok ? "Offer rejected." : "Offer could not be rejected.");
            refreshOffers();
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }
}
