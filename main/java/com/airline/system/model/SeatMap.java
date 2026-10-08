package com.airline.system.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeatMap {
    private int rows;
    private int cols;
    private int[][] seats; // 0 = Free, 1 = Booked

    // Constructor: Sirf Strings ki list lega
    public SeatMap(int rows, int cols, List<String> bookedSeatNumbers) {
        this.rows = rows;
        this.cols = cols;
        this.seats = new int[rows][cols];

        for (String seatNo : bookedSeatNumbers) {
            try {
                // Logic: "10A" -> Row 9, Col 0
                // 1. Row nikalo (Numbers part)
                String rowPart = seatNo.replaceAll("\\D+", ""); 
                int r = Integer.parseInt(rowPart) - 1; // "1" -> Index 0
                
                // 2. Column nikalo (Letters part)
                String colPart = seatNo.replaceAll("\\d+", "");
                char cChar = colPart.charAt(0);
                int c = cChar - 'A'; // 'A' -> Index 0

                // 3. Array mein mark karo
                if (r >= 0 && r < rows && c >= 0 && c < cols) {
                    seats[r][c] = 1; // 1 Matlab Booked
                }
            } catch (Exception e) {
                System.out.println("Skipping invalid seat: " + seatNo);
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public int[][] getSeats() {
        return seats;
    }

    public void setSeats(int[][] seats) {
        this.seats = seats;
    }

    // Frontend Helper: Red color karne k liye list wapis do
    public Set<String> getBookedSeatsSet() {
        Set<String> set = new HashSet<>();
        for(int i=0; i<rows; i++) {
            for(int j=0; j<cols; j++) {
                if(seats[i][j] == 1) {
                    char colChar = (char) ('A' + j);
                    set.add((i + 1) + "" + colChar);
                }
            }
        }
        return set;
    }

    // Logic Helper
    public boolean isAvailable(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= cols) return false;
        return seats[r][c] == 0;
    }
}