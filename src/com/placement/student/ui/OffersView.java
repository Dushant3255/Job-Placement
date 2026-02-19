package com.placement.student.ui;

import com.placement.student.controller.OfferController;
import com.placement.student.dao.OfferDAO;
import com.placement.student.dao.OfferDAOImpl;
import com.placement.student.model.Offer;
import com.placement.student.service.OfferService;
import com.placement.student.service.ServiceException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class OffersView extends JPanel {

    private final long studentId;
    private final OfferController offerController;

    private final DefaultTableModel model;
    private final JTable table;

    private final CardLayout card = new CardLayout();
    private final JPanel tableOrEmpty = new JPanel(card);

    // Filters
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "PENDING", "ACCEPTED", "REJECTED"});
    private List<Offer> cache = new ArrayList<>();

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
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(StudentTheme.BG);

        JLabel title = new JLabel("Offers for Student ID: " + studentId);
        title.setFont(StudentTheme.fontBold(13));
        title.setForeground(new Color(31, 41, 55));
        bar.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(StudentTheme.BG);

        styleCombo(statusFilter);

        JButton clearBtn = new JButton("Clear");
        JButton refreshBtn = new JButton("Refresh");
        StudentTheme.styleSecondaryButton(clearBtn);
        StudentTheme.styleSecondaryButton(refreshBtn);

        right.add(new JLabel("Status"));
        right.add(statusFilter);
        right.add(clearBtn);
        right.add(refreshBtn);

        statusFilter.addActionListener(e -> applyFilters());
        clearBtn.addActionListener(e -> {
            statusFilter.setSelectedItem("All");
            applyFilters();
        });
        refreshBtn.addActionListener(e -> refreshOffers());

        bar.add(right, BorderLayout.EAST);
        return bar;
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
            cache = offerController.viewOffers(studentId);
            applyFilters();
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void applyFilters() {
        String statusSel = String.valueOf(statusFilter.getSelectedItem());

        model.setRowCount(0);
        for (Offer o : cache) {
            if (!"All".equalsIgnoreCase(statusSel)) {
                if (o.getStatus() == null || !o.getStatus().equalsIgnoreCase(statusSel)) continue;
            }

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

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(StudentTheme.fontBold(12));
        combo.setBackground(new Color(243, 244, 246));
        combo.setForeground(new Color(31, 41, 55));
        combo.setBorder(new EmptyBorder(6, 8, 6, 8));
        combo.setFocusable(false);
    }
}
