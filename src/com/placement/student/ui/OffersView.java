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

public class OffersView extends BaseFrame {

    private final long studentId;
    private final OfferController offerController;

    private final DefaultTableModel model;
    private final JTable table;

    public OffersView(long studentId) {
        super("My Offers");
        this.studentId = studentId;

        // light wiring
        OfferDAO offerDAO = new OfferDAOImpl();
        OfferService offerService = new OfferService(offerDAO);
        this.offerController = new OfferController(offerService);

        model = new DefaultTableModel(new Object[]{"Offer ID", "Application ID", "Package (LPA)", "Joining Date", "Status", "Issued At"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);

        add(buildTopBar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        refreshOffers();

        setVisible(true);
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Offers for Student ID: " + studentId);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        p.add(title, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshOffers());
        p.add(refreshBtn, BorderLayout.EAST);

        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return p;
    }

    private JPanel buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton acceptBtn = new JButton("Accept");
        JButton rejectBtn = new JButton("Reject");
        JButton closeBtn = new JButton("Close");

        acceptBtn.addActionListener(e -> acceptSelected());
        rejectBtn.addActionListener(e -> rejectSelected());
        closeBtn.addActionListener(e -> dispose());

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
                        o.getPackageLpa(),
                        o.getJoiningDate(),
                        o.getStatus(),
                        o.getIssuedAt()
                });
            }
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
