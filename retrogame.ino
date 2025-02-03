
#include "LedControl.h"
#include <LiquidCrystal.h>

LedControl lc = LedControl(12, 10, 11, 1);  // DIN, CLK, CS
const int X_pin = 0;
const int buzzerPin = 8;
const int swPin = 9; // Pin del botón del joystick
const int rs = 7, en = 6, d4 = 5, d5 = 4, d6 = 3, d7 = 2;
LiquidCrystal lcd(rs, en, d4, d5, d6, d7);

int ledX = 4, ledY = 4;
int score = 0;
const int maxTargets = 5;
int targetsX[maxTargets], targetsY[maxTargets];
int remainingTargets = maxTargets;
unsigned long startTime = 0;
int moveDelay = 500; 
int obstacleX = 0; 
bool gameRunning = false;

void generateTargets();
void updateLCD();
void collisionSound();
void gameOverSound();
void startGame();

void setup() {
  randomSeed(analogRead(0));
  lc.shutdown(0, false);
  lc.setIntensity(0, 8);
  lc.clearDisplay(0);
  
  lcd.begin(16, 2);
  lcd.print("Press to start!");
  
  pinMode(swPin, INPUT_PULLUP);

  updateLCD();
}

void loop() {
  if (!digitalRead(swPin) && !gameRunning) {
    delay(10);
    if (!digitalRead(swPin)) {
      startGame();
    }
  }

  if (!gameRunning) return;

  if (remainingTargets == 0) {
    moveDelay -= 50; 
    if (moveDelay < 100) moveDelay = 100;
    generateTargets();
  }

  lc.clearDisplay(0);

  int xVal = analogRead(X_pin);
  if (xVal > 1000) ledX = (ledX + 1) % 8;
  if (xVal < 100) ledX = (ledX - 1 + 8) % 8;

  ledY--;
  if (ledY < 0) ledY = 7;

  lc.setLed(0, ledY, ledX, true);
  
  for (int i = 0; i < maxTargets; i++) {
    if (targetsX[i] != -1) {
      lc.setLed(0, targetsY[i], targetsX[i], true);
      if (ledX == targetsX[i] && ledY == targetsY[i]) {
        score += 10;
        targetsX[i] = -1;
        targetsY[i] = -1;
        remainingTargets--;
        collisionSound(); 
      }
    }
  }

  lc.setLed(0, 0, obstacleX, true);
  lc.setLed(0, 0, (obstacleX + 1) % 8, true);
  if (score > 200) {
    if ((ledX == obstacleX && ledY == 0) || (ledX == (obstacleX + 1) % 8 && ledY == 0)) {
      gameRunning = false;
      lcd.clear();
      lcd.setCursor(0, 0);
      lcd.print("GAME OVER");
      lcd.setCursor(0, 1);
      lcd.print("Score: ");
      lcd.print(score);
      gameOverSound();
      delay(2000);
      lcd.setCursor(0, 0);
      lcd.print("Press to start!");
      return;
    }

    obstacleX = (obstacleX + 1) % 8; 
  }

  updateLCD();
  delay(moveDelay);
}

void generateTargets() {
  for (int i = 0; i < maxTargets; i++) {
    targetsX[i] = random(8);
    do {
      targetsY[i] = random(8);
    } while (targetsY[i] == 0);  // Esto asegura que el target nunca esté en la fila 0
  }
  remainingTargets = maxTargets;
}

void updateLCD() {
  unsigned long elapsedTime = (millis() - startTime) / 1000; 
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Score: ");
  lcd.print(score);
  lcd.setCursor(0, 1);
  lcd.print("Time: ");
  lcd.print(elapsedTime);
  lcd.print("s");
}

void collisionSound() {
  tone(buzzerPin, 2000, 100); 
  delay(100);
  noTone(buzzerPin);
}

void gameOverSound() {
  for (int i = 0; i < 3; i++) {
    tone(buzzerPin, 1000 - (i * 200), 200);
    delay(220);
  }
  noTone(buzzerPin);
}

void startGame() {
  gameRunning = true;
  ledX = 4;
  ledY = 4;
  score = 0;
  moveDelay = 500;  
  generateTargets();
  startTime = millis();
  updateLCD();
}
