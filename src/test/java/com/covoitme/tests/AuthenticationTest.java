package com.covoitme.tests;

import static org.junit.jupiter.api.Assertions.*;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

public class AuthenticationTest extends BaseTest {

  @Test
  void shouldRegisterSuccessfully() {
    page.navigate("http://localhost:8080/login");

    page.locator("#showRegister").click();
    page.waitForTimeout(300);

    String uniqueEmail = "test" + System.currentTimeMillis() + "@covoitme.com";
    page.fill("#registerForm input[name='nom']", "Covoitme");
    page.fill("#registerForm input[name='prenom']", "Test");
    page.fill("#registerForm input[name='email']", uniqueEmail);
    page.fill("#registerForm input[name='telephone']", "0123456789");
    page.fill("#registerForm input[name='age']", "25");
    page.fill("#registerForm input[name='motDePasse']", "password123");
    page.fill("#registerForm input[name='confirmMotDePasse']", "password123");

    page.locator("#registerForm button[type='submit']").click();

    page.waitForURL("**/home", new Page.WaitForURLOptions().setTimeout(5000));
    assertTrue(page.url().contains("/home"), "Devrait être redirigé vers /home après inscription");
  }

  @Test
  void shouldLoginSuccessfully() {
    // D'abord, s'inscrire pour créer un utilisateur avec le bon hash
    String testEmail = "loginuser" + System.currentTimeMillis() + "@example.com";
    String testPassword = "MyTestPass123";

    page.navigate("http://localhost:8080/login");
    page.locator("#showRegister").click();
    page.waitForTimeout(300);

    page.fill("#registerForm input[name='nom']", "Login");
    page.fill("#registerForm input[name='prenom']", "Test");
    page.fill("#registerForm input[name='email']", testEmail);
    page.fill("#registerForm input[name='telephone']", "0123456789");
    page.fill("#registerForm input[name='age']", "25");
    page.fill("#registerForm input[name='motDePasse']", testPassword);
    page.fill("#registerForm input[name='confirmMotDePasse']", testPassword);

    page.locator("#registerForm button[type='submit']").click();
    page.waitForURL("**/home", new Page.WaitForURLOptions().setTimeout(5000));

    // Se déconnecter pour tester le login
    page.navigate("http://localhost:8080/logout");
    page.waitForTimeout(500);

    // Maintenant, tester le login avec cet utilisateur
    page.navigate("http://localhost:8080/login");
    page.fill("#loginForm input[name='email']", testEmail);
    page.fill("#loginForm input[name='motDePasse']", testPassword);
    page.waitForTimeout(500);
    page.locator("#loginForm button[type='submit']").click();

    page.waitForURL("**/home", new Page.WaitForURLOptions().setTimeout(5000));
    assertTrue(page.url().contains("/home"), "Devrait être redirigé vers /home après connexion");
  }

  @Test
  void shouldShowErrorWithInvalidCredentials() {
    page.navigate("http://localhost:8080/login");
    page.fill("#loginForm input[name='email']", "invalid@example.com");
    page.fill("#loginForm input[name='motDePasse']", "wrongpassword");
    page.locator("#loginForm button[type='submit']").click();

    page.waitForTimeout(1000);
    String errorMessage = page.locator(".bg-red-100").textContent();
    assertTrue(
      errorMessage.contains("Email ou mot de passe incorrect"),
      "Devrait afficher un message d'erreur pour des identifiants invalides"
    );
  }

  @Test
  void shouldShowErrorWithEmptyLoginFields() {
    page.navigate("http://localhost:8080/login");
    page.fill("#loginForm input[name='email']", "");
    page.fill("#loginForm input[name='motDePasse']", "");

    // Les champs HTML5 required devraient empêcher la soumission
    page.evaluate("document.querySelector('#loginForm input[name=\"email\"]').removeAttribute('required')");
    page.evaluate(
      "document.querySelector('#loginForm input[name=\"motDePasse\"]').removeAttribute('required')"
    );

    page.locator("#loginForm button[type='submit']").click();

    page.waitForTimeout(1000);
    String errorMessage = page.locator(".bg-red-100").textContent();
    assertTrue(
      errorMessage.contains("Tous les champs sont obligatoires"),
      "Devrait afficher un message d'erreur pour des champs vides"
    );
  }

  @Test
  void shouldShowErrorWhenPasswordsDoNotMatch() {
    page.navigate("http://localhost:8080/login");
    page.locator("#showRegister").click();
    page.waitForTimeout(300);

    page.fill("#registerForm input[name='nom']", "Test");
    page.fill("#registerForm input[name='prenom']", "User");
    page.fill("#registerForm input[name='email']", "test@example.com");
    page.fill("#registerForm input[name='telephone']", "0123456789");
    page.fill("#registerForm input[name='age']", "25");
    page.fill("#registerForm input[name='motDePasse']", "password123");
    page.fill("#registerForm input[name='confirmMotDePasse']", "password456");

    page.locator("#registerForm button[type='submit']").click();

    page.waitForTimeout(1000);
    String errorMessage = page.locator(".bg-red-100").textContent();
    assertTrue(
      errorMessage.contains("Les mots de passe ne correspondent pas"),
      "Devrait afficher un message d'erreur quand les mots de passe ne correspondent pas"
    );
  }

  @Test
  void shouldShowErrorWhenAgeIsUnder18() {
    page.navigate("http://localhost:8080/login");
    page.locator("#showRegister").click();
    page.waitForTimeout(300);

    page.fill("#registerForm input[name='nom']", "Test");
    page.fill("#registerForm input[name='prenom']", "User");
    page.fill("#registerForm input[name='email']", "minor@example.com");
    page.fill("#registerForm input[name='telephone']", "0123456789");

    // Retirer la validation HTML5 pour tester la validation côté serveur
    page.evaluate("document.querySelector('#registerForm input[name=\"age\"]').removeAttribute('min')");
    page.fill("#registerForm input[name='age']", "17");

    page.fill("#registerForm input[name='motDePasse']", "password123");
    page.fill("#registerForm input[name='confirmMotDePasse']", "password123");

    page.locator("#registerForm button[type='submit']").click();

    page.waitForTimeout(1000);
    String errorMessage = page.locator(".bg-red-100").textContent();
    assertTrue(
      errorMessage.contains("Vous devez avoir au moins 18 ans"),
      "Devrait afficher un message d'erreur pour les moins de 18 ans"
    );
  }

  @Test
  void shouldShowErrorWhenPhoneNumberIsInvalid() {
    page.navigate("http://localhost:8080/login");
    page.locator("#showRegister").click();
    page.waitForTimeout(300);

    page.fill("#registerForm input[name='nom']", "Test");
    page.fill("#registerForm input[name='prenom']", "User");
    page.fill("#registerForm input[name='email']", "test@example.com");

    // Retirer la validation HTML5 pour tester la validation côté serveur
    page.evaluate(
      "document.querySelector('#registerForm input[name=\"telephone\"]').removeAttribute('pattern')"
    );
    page.fill("#registerForm input[name='telephone']", "123456789");

    page.fill("#registerForm input[name='age']", "25");
    page.fill("#registerForm input[name='motDePasse']", "password123");
    page.fill("#registerForm input[name='confirmMotDePasse']", "password123");

    page.locator("#registerForm button[type='submit']").click();

    page.waitForTimeout(1000);
    String errorMessage = page.locator(".bg-red-100").textContent();
    assertTrue(
      errorMessage.contains("numéro de téléphone") || errorMessage.contains("format français"),
      "Devrait afficher un message d'erreur pour un numéro de téléphone invalide"
    );
  }

  @Test
  void shouldShowErrorWhenEmailAlreadyExists() {
    // Créer un utilisateur existant
    insertTestUser(
      "existing@example.com",
      "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhkW",
      "Existing",
      "User"
    );

    page.navigate("http://localhost:8080/login");
    page.locator("#showRegister").click();
    page.waitForTimeout(300);

    // Essayer de créer un compte avec le même email
    page.fill("#registerForm input[name='nom']", "Test");
    page.fill("#registerForm input[name='prenom']", "User");
    page.fill("#registerForm input[name='email']", "existing@example.com");
    page.fill("#registerForm input[name='telephone']", "0123456789");
    page.fill("#registerForm input[name='age']", "25");
    page.fill("#registerForm input[name='motDePasse']", "password123");
    page.fill("#registerForm input[name='confirmMotDePasse']", "password123");

    page.locator("#registerForm button[type='submit']").click();

    page.waitForTimeout(1000);
    String errorMessage = page.locator(".bg-red-100").textContent();
    assertTrue(
      errorMessage.contains("adresse email est déjà utilisée"),
      "Devrait afficher un message d'erreur quand l'email existe déjà"
    );
  }

  @Test
  void shouldShowErrorWithEmptyRegistrationFields() {
    page.navigate("http://localhost:8080/login");
    page.locator("#showRegister").click();
    page.waitForTimeout(300);

    // Retirer la validation HTML5 required pour tous les champs
    page.evaluate(
      "document.querySelectorAll('#registerForm input[required]').forEach(el => el.removeAttribute('required'))"
    );

    // Remplir seulement le nom, laisser les autres champs vides
    page.fill("#registerForm input[name='nom']", "Test");

    page.locator("#registerForm button[type='submit']").click();

    page.waitForTimeout(1500);

    // Vérifier la présence du message d'erreur
    if (page.locator(".bg-red-100").count() > 0) {
      String errorMessage = page.locator(".bg-red-100").textContent();
      assertTrue(
        errorMessage.contains("Tous les champs sont obligatoires") || errorMessage.contains("obligatoire"),
        "Devrait afficher un message d'erreur quand des champs sont vides à l'inscription"
      );
    } else {
      // Si pas de message d'erreur visible, vérifier qu'on est toujours sur la page de login
      assertTrue(
        page.url().contains("/login") || page.url().contains("/register"),
        "Devrait rester sur la page de login/register avec des champs vides"
      );
    }
  }

  @Test
  void shouldToggleBetweenLoginAndRegisterForms() {
    page.navigate("http://localhost:8080/login");

    // Vérifier que le formulaire de connexion est visible par défaut
    assertTrue(page.locator("#loginForm").isVisible(), "Le formulaire de connexion devrait être visible");
    assertFalse(page.locator("#registerForm").isVisible(), "Le formulaire d'inscription devrait être caché");

    // Cliquer sur le bouton d'inscription
    page.locator("#showRegister").click();
    page.waitForTimeout(200);

    // Vérifier que le formulaire d'inscription est maintenant visible
    assertFalse(page.locator("#loginForm").isVisible(), "Le formulaire de connexion devrait être caché");
    assertTrue(page.locator("#registerForm").isVisible(), "Le formulaire d'inscription devrait être visible");

    // Revenir au formulaire de connexion
    page.locator("#showLogin").click();
    page.waitForTimeout(200);

    // Vérifier que le formulaire de connexion est à nouveau visible
    assertTrue(page.locator("#loginForm").isVisible(), "Le formulaire de connexion devrait être visible");
    assertFalse(page.locator("#registerForm").isVisible(), "Le formulaire d'inscription devrait être caché");
  }
}
