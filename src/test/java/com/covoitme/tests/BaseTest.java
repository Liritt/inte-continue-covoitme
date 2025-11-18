package com.covoitme.tests;

import com.microsoft.playwright.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseTest {

  protected static Playwright playwright;
  protected static Browser browser;
  protected BrowserContext context;
  protected Page page;

  @BeforeAll
  static void launchBrowser() {
    playwright = Playwright.create();
    browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(500));
  }

  @AfterAll
  static void closeBrowser() {
    browser.close();
    playwright.close();
  }

  @BeforeEach
  void createContextAndPage() {
    context = browser.newContext();
    page = context.newPage();
  }

  @AfterEach
  void closeContext() {
    context.close();
  }

  protected void insertTestUser(String email, String password, String nom, String prenom) {
    String url = "jdbc:postgresql://localhost:5432/covoitme";
    String user = "covoitme";
    String dbPassword = "password";

    try (Connection conn = DriverManager.getConnection(url, user, dbPassword)) {
      // Vérifier si l'utilisateur existe déjà
      String checkQuery = "SELECT COUNT(*) FROM utilisateur WHERE email = ?";
      try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
        checkStmt.setString(1, email);
        var rs = checkStmt.executeQuery();
        if (rs.next() && rs.getInt(1) > 0) {
          return; // L'utilisateur existe déjà
        }
      }

      // Insérer l'utilisateur
      String insertQuery =
        "INSERT INTO utilisateur (nom, prenom, email, numTel, age, password) VALUES (?, ?, ?, ?, ?, ?)";
      try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
        stmt.setString(1, nom);
        stmt.setString(2, prenom);
        stmt.setString(3, email);
        stmt.setString(4, "0123456789");
        stmt.setInt(5, 25);
        stmt.setString(6, password);
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Erreur lors de l'insertion de l'utilisateur de test", e);
    }
  }
}
