package edu.cwi;

import java.math.BigDecimal;

public record Transaction(int id, String description, BigDecimal amount) {
}
