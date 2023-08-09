import javafx.animation.*;
import javafx.application.Application;

import java.io.Serializable;

import javafx.application.Platform;
import javafx.stage.WindowEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.Timeline;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.control.Spinner;
import javafx.scene.control.Label;
import javafx.geometry.Insets;


public class GuiClient extends Application {

	private Alert alertRules;
	private pokerInfo currentPokerInfo;
	private List<Deck.Card> dealerHand;
	private List<Deck.Card> playerHand;
	Client clientConnection = null;
	ListView<String> listItems2;

	TextField portNumField, ipAddrField, gameInfoWinning;
	Button connectButton, playButton, dealButton, playWager, foldButton;
	private Stage primaryStage;
	Scene startScene, gameScene;
	BorderPane startPane, layout;
	MenuBar menuBar;
	BackgroundImage gameBgImage;
	HBox playerCards, dealerCards;
	VBox playerHandBox, dealerHandBox;
	ImageView playerCard1, playerCard2, playerCard3, dealerCard1, dealerCard2, dealerCard3;
	Image hiddenCard = new Image("https://bicyclecards.org/wp-content/uploads/2019/11/red-56.jpg");
	Button playAgain, exitGame;
	TextField endMessageText, gameMoneyText, totalMoneyText;
	Spinner<Integer> anteSpinner, pairPlusSpinner;
	CheckBox pairPlusCheck;
	AtomicInteger countDown;
	int anteValue = 0, pairPlusValue = 0;


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		this.primaryStage = primaryStage; //To transition to other methods
		primaryStage.setTitle("3 Card Poker Game");

		// Port Number and IP Address fields
		portNumField = new TextField();
		portNumField.setPromptText("Port Number");
		portNumField.setMaxWidth(200);

		ipAddrField = new TextField();
		ipAddrField.setPromptText("IP Address");
		ipAddrField.setMaxWidth(200);

		// Play Button is set to disable at the beginning
		// User will type in port number and ip address, then press on connectButton
		// create a client server socket with valid ip & port
		connectButton = new Button("Connect");
		playButton = new Button("Play 3 Card Poker");
		playButton.setDisable(true);

		listItems2 = new ListView<>();

		//Handles initial Client server connection
		connectButton.setOnAction(e -> {connectButtonHandler();});

		primaryStage.setOnCloseRequest(t -> {
			Platform.exit();
			System.exit(0);
		});

		VBox startVBox = new VBox(portNumField, ipAddrField, connectButton, playButton);
		startVBox.setAlignment(Pos.CENTER);
		startVBox.setSpacing(10);

		startPane = new BorderPane();
		startPane.setCenter(startVBox);

		// Button to start playing the game
		playButton.setOnAction(e -> startPoker());

		// Sets the background of the start scene to an image
		Image pokerWelcomeImg = new Image("https://www.casino.org/blog/wp-content/uploads/3-card-poker.png");
		BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, true, true);
		BackgroundImage startImage = new BackgroundImage(pokerWelcomeImg, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
				BackgroundPosition.CENTER, backgroundSize);
		startPane.setBackground(new Background(startImage));
		startPane.setStyle("-fx-font-family: Arial; -fx-font-size: 12");

		// Sets new scene
		startScene = new Scene(startPane, 1400, 800);
		primaryStage.setScene(startScene);
		primaryStage.show();
	}

	// The Game Scene
	// Called after clicking on play button
	private void startPoker() {
		// Setting game values to zero at the beginning
		anteValue = 0;
		pairPlusValue = 0;
		layout = new BorderPane();
		menuBar = new MenuBar();

		//Menu Bar Setup
		Menu menu = new Menu("Options");
		Menu menu2 = new Menu("Rules");
		menu.setStyle("-fx-font-family: Arial; -fx-font-size: 18");
		MenuItem freshStart = new MenuItem("Fresh Start");

		// Rets total winnings and starts the game over again
		freshStart.setOnAction(e -> {
			currentPokerInfo.gameWinnings = 0;
			startNewGame();
			clientConnection.send(currentPokerInfo);
			countDown.set(0);
			startPoker();
		});

		// Sets a new look for the play scene
		MenuItem newLook = new MenuItem("New Look");
		newLook.setOnAction(e -> newLook());

		// Exits the play scene
		MenuItem exit = new MenuItem("Exit");
		exit.setOnAction(e -> this.primaryStage.close());

		// Displays the rules of the game
		MenuItem rules = new MenuItem("How to Play");
		rules.setOnAction(e -> {
			alertRules = new Alert(Alert.AlertType.INFORMATION);
			alertRules.setTitle("Poker game - Rules");
			alertRules.setHeaderText("How to Play 3 Card poker");
			alertRules.setContentText("1. Click start to begin. \n"
					+ "2. Place a ante wager between 5$ and 25$.\n"
					+ "3. Place an optional pair plus wager between 5$ and 25$.\n"
					+ "4. Click deals and your playing cards will show up! \n"
					+ "5. Evaluate and decide if you want to fold or play the wager.\n"
					+ "6. See if your playing cards beat the dealer.\n"
					+ "7. After 10 seconds, your game results and winnings will appear.\n"
			);
			alertRules.showAndWait();
		});

		menu.getItems().addAll(freshStart, newLook, exit);
		menu2.getItems().add(rules);
		menuBar.getMenus().add(menu);
		menuBar.getMenus().add(menu2);


		// Make ante and pair plus labels
		// Have a spinner(button type) for user to select number
		HBox AntePairPlus = new HBox(10);
		Label ante = new Label("Ante: ");
		ante.setStyle("-fx-background-color: linear-gradient(#87CEEB, #FFFFFF);" +
				"-fx-background-radius: 5px; ");
		ante.setAlignment(Pos.CENTER);
		ante.setMaxWidth(50);
		anteSpinner = new Spinner<>(5, 25, 1);
		anteSpinner.setMaxWidth(100);

		Label pairPlus = new Label("Pair Plus: ");
		pairPlus.setStyle("-fx-background-color: linear-gradient(#87CEEB, #FFFFFF);" +
				"-fx-background-radius: 5px; ");
		pairPlus.setAlignment(Pos.CENTER);
		pairPlus.setMaxWidth(150);
		pairPlusCheck = new CheckBox("Select pairPlus");
		pairPlusSpinner = new Spinner<>(5, 25, 1);
		pairPlusSpinner.setEditable(false);
		pairPlusSpinner.setMaxWidth(100);
		pairPlusCheck.selectedProperty().addListener((observable, unChecked, checked) -> {
			pairPlusSpinner.setDisable(!checked);
		});
		pairPlusSpinner.setDisable(true);

		AntePairPlus.getChildren().addAll(ante, anteSpinner, pairPlus, pairPlusCheck, pairPlusSpinner);
		AntePairPlus.setAlignment(Pos.CENTER);
		AntePairPlus.setStyle("-fx-font-family: Arial; -fx-font-size: 18");


		// Creating dealer/player layout
		// Displays total winnings at the top
		gameInfoWinning = new TextField();
		gameInfoWinning.setText("TOTAL WINNINGS:" + currentPokerInfo.gameWinnings);
		gameInfoWinning.setMaxWidth(300);
		gameInfoWinning.setDisable(true);

		// Dealer Hand set at top center of the screen
		Label dealerHandLabel = new Label("DEALER HAND");
		dealerHandLabel.setAlignment(Pos.CENTER);
		dealerHandLabel.setMaxWidth(300);
		dealerHandLabel.setStyle("-fx-background-color: linear-gradient(to bottom, #87CEEB, #FFFFFF); " +
				"-fx-background-radius: 5px; " +
				"-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 10, 0, 0, 0);");

		// Player Hand set at bottom center of the scene
		playerHandBox = new VBox(50);
		Label playerHandLabel = new Label("YOUR HAND: PLAYER ONE");
		playerHandLabel.setAlignment(Pos.CENTER);
		playerHandLabel.setMaxWidth(500);
		playerHandLabel.setStyle("-fx-background-color: linear-gradient(to bottom, #87CEEB, #FFFFFF); " +
				"-fx-background-radius: 5px; " +
				"-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 10, 0, 0, 0);");

		// Deal button records the ante and pair plus that the user selects
		// Sends that info to the server
		// Disables ante and pair plus selections, and enables playWager and fold buttons
		// Shows the player cards retrieved from the server
		dealButton = new Button("Deal");
		dealButton.setOnAction(e -> { dealButtonHandler();});
		playerCard1 = new ImageView();
		playerCard2 = new ImageView();
		playerCard3 = new ImageView();
		playerCards = createCardBox(hiddenCard, playerCard1, playerCard2, playerCard3);
		dealerCard1 = new ImageView();
		dealerCard2 = new ImageView();
		dealerCard3 = new ImageView();
		dealerCards = createCardBox(hiddenCard, playerCard1, playerCard2, playerCard3);

		// Sets player display
		playerHandBox.getChildren().addAll(playerCards, playerHandLabel, dealButton, AntePairPlus);
		playerHandBox.setMargin(AntePairPlus, new Insets(0, 0, 50, 0));
		playerHandBox.setSpacing(10);
		playerHandBox.setAlignment(Pos.CENTER);

		// Sets dealer display
		dealerHandBox = new VBox(10);
		dealerHandBox.getChildren().addAll(menuBar, gameInfoWinning, dealerHandLabel, dealerCards);
		dealerHandBox.setAlignment(Pos.CENTER);

		HBox playerOptions = new HBox(10);
		playWager = new Button("Play Wager: ");
		playWager.setDisable(true);
		foldButton = new Button("Fold");
		foldButton.setDisable(true);

		playerOptions.getChildren().addAll(playWager, foldButton);
		playerOptions.setAlignment(Pos.CENTER);

		//Implementing object output stream here to send to Server once clicked
		playWager.setOnAction(e -> {
			currentPokerInfo.playerFolded = false;
			gameMoveHandler();
		});

		// pokerInfo object is sent to server
		foldButton.setOnAction(e-> {
			currentPokerInfo.playerFolded = true;
			gameMoveHandler();
		});

		// Sets the background of the game scene to an image
		this.layout.setBottom(playerHandBox);
		this.layout.setTop(dealerHandBox);
		this.layout.setCenter(playerOptions);

		// Sets background image of the play scene
		Image gameSceneImage = new Image("https://img.freepik.com/free-vector/gradient-background-green-tones_23-2148374530.jpg");
		BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, true, true);
		gameBgImage = new BackgroundImage(gameSceneImage, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
				BackgroundPosition.CENTER, backgroundSize);
		this.layout.setBackground(new Background(gameBgImage));
		this.layout.setStyle("-fx-font-family: Arial; -fx-font-size: 18");

		//Scene setup
		gameScene = new Scene(layout, 1400, 800);
		this.primaryStage.setScene(this.gameScene);

		//Fade Transition from main menu to game scene
		FadeTransition infade = new FadeTransition(Duration.millis(500), startScene.getRoot());
		infade.setFromValue(1.0);
		infade.setToValue(0.0);

		FadeTransition outfade = new FadeTransition(Duration.millis(500), gameScene.getRoot());
		outfade.setFromValue(0.0);
		outfade.setToValue(1.0);

		infade.setOnFinished(e -> {
			primaryStage.setScene(gameScene);
			outfade.play();
		});

		SequentialTransition fadeInAndOut = new SequentialTransition(infade, outfade);
		fadeInAndOut.play();
	}

	// New look for the game scene
	private void newLook() {
		// Changes the background color from green to red
		Image gameSceneImage = new Image("https://img.freepik.com/premium-vector/hand-painted-watercolor-abstract-watercolor-background_23-2149024912.jpg");
		BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, true, true);
		gameBgImage = new BackgroundImage(gameSceneImage, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
				BackgroundPosition.CENTER, backgroundSize);
		this.layout.setBackground(new Background(gameBgImage));
	}

	// Returns HBox of 3 cards aligned together
	// Used to display player and dealer cards
	private HBox createCardBox(Image cardImage, ImageView... cards) {
		HBox cardBox = new HBox(5);

		for (ImageView card : cards) {
				card = new ImageView(cardImage);
				card.setFitWidth(150);
				card.setFitHeight(150);
				cardBox.getChildren().add(card);
			}

		cardBox.setAlignment(Pos.CENTER);
		return cardBox;
	}

	// Event handler for connectButton
	// Retrieves ip address and port number that user enters
	// Creates a connection with server
	private void connectButtonHandler(){
		// Get the user input for IP address and port number
		String ipAddress = ipAddrField.getText();
		int portNum;
		try {
			portNum = Integer.parseInt(portNumField.getText());
		} catch (NumberFormatException ex) {
			// Handle the exception, e.g., show an error message to the user
			System.err.println("Invalid port number format");
			return;
		}
		currentPokerInfo = new pokerInfo();
		currentPokerInfo.requestCards = true;

		// Retrieved the pokerInfo object sent by the server
		try {
			clientConnection = new Client((Serializable info) -> {
				Platform.runLater(() -> {
					currentPokerInfo = (pokerInfo) info;
					this.dealerHand = ((pokerInfo) info).dealerHand;
					this.playerHand = ((pokerInfo) info).playerHand;
				});
			}, ipAddress, portNum); // Pass the ipAddress and portNum to the Client constructor
			startNewGame();
			currentPokerInfo.gameWinnings = 0;
			// Sends pokerInfo to server and retrieves it with updates
			clientConnection.start();
			clientConnection.send(currentPokerInfo);
			playButton.setDisable(false);
			connectButton.setDisable(true);
		} catch (IllegalArgumentException ex) {
			// Handle the exception, e.g., show an error message to the user
			System.err.println(ex.getMessage());
		}
	}

	// Event handler for deal button
	// Retrieves ante and pair plus number that the user selects
	// Displays player cards
	private void dealButtonHandler() {
		// Get value from ante and disable it
		anteValue = anteSpinner.getValue();
		currentPokerInfo.ante = anteValue;
		anteSpinner.setDisable(true);

		// Get value from pair plus and disable it
		pairPlusValue = pairPlusSpinner.isDisabled()? 0: pairPlusSpinner.getValue();
		currentPokerInfo.pairPlus = pairPlusValue;

		pairPlusSpinner.setDisable(true);
		pairPlusCheck.setDisable(true);
		dealButton.setDisable(true);

		// Enable player moves: play wager and fold button
		playWager.setDisable(false);
		playWager.setText("Play Wager: " + anteValue);
		foldButton.setDisable(false);
		displayCards(playerHand, playerCards, playerCard1, playerCard2, playerCard3);
	}

	// Sets the card images to the images sent from server
	// Adds a fade transition for the cards to appear
	private void displayCards(List<Deck.Card> images, HBox setOfCards, ImageView... imageViews) {

		for (int i = 0; i < imageViews.length; i++) {
			// For each card, image path is added
			ImageView imageView = imageViews[i];
			Deck.Card card = images.get(i);
			Image cardImage = new Image(card.getImage());
			imageView.setImage(cardImage);
			imageView.setFitWidth(150);
			imageView.setFitHeight(150);

			// Animation to display the cards
			FadeTransition fadeIn = new FadeTransition(Duration.millis(500), imageView);
			fadeIn.setFromValue(1.0);
			fadeIn.setToValue(0.0);

			FadeTransition fadeOut = new FadeTransition(Duration.millis(500), imageView);
			fadeOut.setFromValue(0.0);
			fadeOut.setToValue(1.0);

			int finalI = i;
			fadeIn.setOnFinished(e -> {
				setOfCards.getChildren().set(finalI, imageView);
			});

			SequentialTransition fadeInAndOut = new SequentialTransition(fadeIn, fadeOut);
			fadeInAndOut.play();
		}
	}

	// Event handler for playWager button
	// Updates server that user wants to play
	// Displays dealer card, updates gameInfo, leads to win/lose scene
	private void gameMoveHandler() {

		// Sends what the player waged / folded
		currentPokerInfo.message ="Player is sending object";
		currentPokerInfo.requestCards = true;
		// Server updates current round winnings and adds it to gameTotalWinnings
		// Also updates if player wins or loses the game
		clientConnection.send(currentPokerInfo);

		playButton.setDisable(false);
		connectButton.setDisable(true);
		playWager.setDisable(true);
		foldButton.setDisable(true);

		// Displays dealer cards after player waged / folded
		displayCards(dealerHand, dealerCards, dealerCard1, dealerCard2, dealerCard3);

		// Counts down 10 seconds before end scene is displayed
		// Given enough time for player to understand dealer cards
		countDown = new AtomicInteger(10);
		Label countDownLabel = new Label("Game Results In: " + countDown.get());
		countDownLabel.setStyle("-fx-font-family: Arial #000000;"
				+ "-fx-background-color: #bbd4f1ff;"
				+ "-fx-font-weight: 700;");
		dealerHandBox.getChildren().add(countDownLabel);

		// Countdown timer
		Timeline countDownTime = new Timeline(
				new KeyFrame(Duration.ZERO, e -> countDownLabel.setText("Game Results In: " + countDown.get())),
				new KeyFrame(Duration.seconds(1), evt -> {
					countDown.getAndDecrement();
					if (countDown.get() == 1) {
						endScene();
					}
				})
		);
		countDownTime.setCycleCount(countDown.get());
		countDownTime.play();
	}


	// The ending scene with if player wins or loses
	void endScene() {

		// Updates the total winnings for the player
		gameInfoWinning.setText("TOTAL WINNINGS: " + currentPokerInfo.gameWinnings);
		dealerHandBox.getChildren().set(1, gameInfoWinning);
		this.layout.setTop(dealerHandBox);

		String endMessage;
		// Money from this round and total money across all rounds
		String moneyMessage = "MONEY THIS ROUND: " + currentPokerInfo.totalWinnings;
		String totalMoneyMessage = "TOTAL MONEY WON: " + currentPokerInfo.gameWinnings;
		if (currentPokerInfo.winsGame) {
			endMessage = "YOU WON THE GAME!!";
		} else {
			endMessage = "YOU LOST THE GAME :/";
		}

		// Option to play the game again
		playAgain = new Button("Play Again");
		playAgain.setStyle("-fx-font-family: Arial");
		playAgain.setOnAction(e->{
			startNewGame();
			clientConnection.send(currentPokerInfo);
			startPoker();
		});

		// Option to exit the game
		exitGame = new Button("Exit");
		exitGame.setStyle("-fx-font-family: Arial");
		exitGame.setOnAction(e->{this.primaryStage.close();});

		// Message displayed for winnings/losings
		endMessageText = new TextField(endMessage);
		endMessageText.setDisable(true);
		endMessageText.setMaxWidth(200);
		endMessageText.setStyle("-fx-font-family: Arial");
		gameMoneyText = new TextField(moneyMessage);
		gameMoneyText.setDisable(true);
		gameMoneyText.setStyle("-fx-font-family: Arial");
		gameMoneyText.setMaxWidth(200);
		totalMoneyText = new TextField(totalMoneyMessage);
		totalMoneyText.setDisable(true);
		totalMoneyText.setStyle("-fx-font-family: Arial");
		totalMoneyText.setMaxWidth(200);

		// Set end scene
		BorderPane endBorder = new BorderPane();
		endBorder.setBackground(new Background(gameBgImage));
		VBox endVBox = new VBox(5);
		endVBox.getChildren().addAll(endMessageText, gameMoneyText, totalMoneyText, playAgain, exitGame);
		endVBox.setAlignment(Pos.CENTER);
		endBorder.setCenter(endVBox);
		Scene endScene = new Scene(endBorder, 1400, 800);

		primaryStage.setScene(endScene);
	}

	// Initializes the pokerInfo object wit empty lists
	// Called before sending it to server
	private void startNewGame() {
		currentPokerInfo.dealerHand = new ArrayList<>();
		currentPokerInfo.playerHand = new ArrayList<>();
		currentPokerInfo.ante = 0;
		currentPokerInfo.pairPlus = 0;
		currentPokerInfo.totalWinnings = 0;
		currentPokerInfo.winsGame = false;
		currentPokerInfo.playerFolded = false;
		currentPokerInfo.message ="Player is sending object";
		currentPokerInfo.requestCards = true;
	}

}