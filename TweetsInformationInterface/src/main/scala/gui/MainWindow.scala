package main.scala.gui

import javafx.application.Application
import javafx.beans.value.ObservableValue
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Label
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TabPane.TabClosingPolicy
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.ComboBox
import javafx.scene.control.ScrollPane
import javafx.scene.control.Button
import javafx.scene.chart.BarChart
import javafx.scene.chart.XYChart
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.Border
import javafx.scene.layout.BorderPane
import javafx.scene.layout.BorderStroke
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.layout.BorderWidths
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.Pane
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.layout.Priority
import javafx.scene.shape.Circle
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.stage.Stage
import javafx.event.ActionEvent
import javafx.stage.WindowEvent
import javafx.event.EventHandler
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType

import main.scala.gui.controller.MainController
import main.scala.gui.controller.TreeController
import main.scala.gui.controller.SearchController


class MainWindow{

    val black = new Border(new BorderStroke(Color.BLACK,
        BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(2)))
    val red = new Border(new BorderStroke(Color.RED,
        BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(2)))
    val blue = new Border(new BorderStroke(Color.BLUE,
        BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(2)))
    val yellow = Color.YELLOW.deriveColor(0, .9, 1, 1)

    
    var labelNombre1 = new Label("")
    var labelPuntPropia1 = new Label("")
    var labelPuntHijos1 = new Label("")
    var labelMenciones1 = new Label("")
    
    var labelNombre2 = new Label("")
    var labelPuntPropia2 = new Label("")
    var labelPuntHijos2 = new Label("")
    var labelMenciones2 = new Label("")
    
    
    def start(primaryStage : Stage, cont : MainController) = {
        primaryStage.setTitle("Hierarchy Manager")

        var tabPane = new TabPane()
        
        var tab1 = new Tab()
        var tab2 = new Tab()
        var tab3 = new Tab()
        
        var root1 = new GridPane()
        root1.setPadding(new Insets(16))
        root1.setVgap(16)
        
        //root1.setBackground(new Background(new BackgroundFill(
        //    Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        
        var topGridPane1 = new GridPane();
        var topGridPane2 = new GridPane();
        
        var mainController = cont
        
        /*
        var top = new BorderPane();
        top.setPadding(new Insets(16));
        top.setBorder(red);
        top.setLeft(createLabel("Label 1", yellow, 16));
        var topCenter = createLabel("Label 2", yellow, 64);
        topCenter.setContentDisplay(ContentDisplay.CENTER);
        BorderPane.setMargin(topCenter, new Insets(16));
        top.setCenter(topCenter);
        top.setRight(createLabel("Label 3", yellow, 16));
        * */

        mainController.initGraph()
        //var center = new Browser("file:///C:/Users/Javier/workspaceScala/Pruebas/src/main/resources/web/prueba.html")
        var center = new Browser("file://" + ClassLoader.getSystemClassLoader().getResource(".").getPath() + "graphResources/prueba.html")
        center.setPadding(new Insets(16))
        topGridPane1.add(center, 1, 0)

        /*
        var bot = new BorderPane();
        bot.setPadding(new Insets(16));
        bot.setBorder(blue);
        bot.setCenter(createLabel("Label 4", Color.GREEN, 24));
        * */
        var treeController = new TreeController(mainController.getJerarquia, mainController.getGraphFileManager, labelNombre1, labelPuntPropia1, labelPuntHijos1, labelMenciones1, center)
        topGridPane1.add(this.treeView(treeController,mainController,null), 0, 0)
        
        root1.add(topGridPane1,0,0)
        root1.add(this.bottomView(true), 0, 1)
        
        
        tab1.setContent(root1)
        
        
        var root2 = new GridPane()
        root2.setPadding(new Insets(16))
        root2.setVgap(16)
        
        var pane = new Pane()
        var treeController2 = new TreeController(mainController.getJerarquia, mainController.getGraphFileManager, labelNombre2, labelPuntPropia2, labelPuntHijos2, labelMenciones2, pane)
        var chartCreator = new ChartCreator(treeController2)
        topGridPane2.add(this.treeView(treeController2,mainController,chartCreator), 0, 0)
        topGridPane2.add(pane,1,0)
        treeController2.updateChart(chartCreator.getDefaultChart())
        topGridPane2.add(this.rigthView(treeController2,chartCreator),2,0)
        
        root2.add(topGridPane2,0,0)
        root2.add(this.bottomView(false), 0, 1)
        tab2.setContent(root2)
        
        var searchController = new SearchController()
        var searchGridPane = new GridPane()
        var scrollPane = new ScrollPane()
        var advSearchWindow = new AdvanceSearchWindow(0)
        advSearchWindow.creaAdvanceSearchWindow(searchController)
        var searchButton = new Button("Buscar")
        searchButton.setOnAction(new EventHandler[ActionEvent]() {
          override def handle(e : ActionEvent) {
            var table = new SearchResultsWindow()
            val resultsSearch = advSearchWindow.ejecutaBusqueda(searchController)
            table.inicializaDosTablas(resultsSearch.filter { x => x.getNumElements()==4 }.asInstanceOf[List[UsualResult]], resultsSearch.filter { x => x.getNumElements()==3 }.asInstanceOf[List[ModifierResult]])
            var stage = new Stage()
            stage.setTitle("Resultados de busqueda")
            stage.setScene(new Scene(table.getVBox, 450, 450))
            stage.show()
          }
        })
        searchGridPane.setPadding(new Insets(16))
        searchGridPane.add(searchButton, 0, 0)
        searchGridPane.add(advSearchWindow.getMainPane, 0, 1)
        scrollPane.setContent(searchGridPane)
        tab3.setContent(scrollPane)
        
        /*
        tab1.setGraphic(createImage("file:src/main/resources/gui/grafo.png"))
        tab2.setGraphic(createImage("file:src/main/resources/gui/grafico.png"))
        tab3.setGraphic(createImage("file:src/main/resources/gui/busqueda.png"))
        * */
        tab1.setGraphic(createImage(getClass().getResource("/main/resources/gui/grafo.png").toString()))
        tab2.setGraphic(createImage(getClass().getResource("/main/resources/gui/grafico.png").toString()))
        tab3.setGraphic(createImage(getClass().getResource("/main/resources/gui/busqueda.png").toString()))
        tabPane.getTabs().add(tab1)
        tabPane.getTabs().add(tab2)
        tabPane.getTabs().add(tab3)
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE)
        tabPane.setTabMaxWidth(50)
        tabPane.setTabMaxHeight(50)
        
        
        var MenuConf = new MenuConf(mainController)
        var menuBar = MenuConf.getMenuConf
        
        VBox.setVgrow(menuBar, Priority.ALWAYS)
        VBox.setVgrow(tabPane, Priority.ALWAYS)
        
        var root = new VBox()
        root.getChildren().addAll(menuBar)
        root.getChildren().addAll(tabPane)
        VBox.setVgrow(root, Priority.ALWAYS)
        
        var scene = new Scene(root)
        primaryStage.setScene(scene)
        primaryStage.show()
    }

    def createLabel(text : String, color : Color, size : Int) : Label = {
        var l = new Label(text)
        l.setContentDisplay(ContentDisplay.TOP)
        l.setTextFill(color)
        l.setFont(new Font(16))
        return l
    }
    
    def createImage(path : String) : ImageView = {
      var image = new Image(path);
      var imageView = new ImageView(path);
      imageView.setFitHeight(50);
      imageView.setFitWidth(50);
      imageView.setImage(image);
      return imageView;
    }
    
    def treeView(treeController : TreeController,mainController : MainController, chartCreator : ChartCreator) : TreeView[TreeItemPair] = {
        var treeCreator = new TreeCreator(treeController)
        var left = treeCreator.createTree(chartCreator)
        left.setPadding(new Insets(16))
        left.setBorder(blue)
        left
    }
    
    def bottomView(primerPanel : Boolean) : GridPane = {
      var bottom = new GridPane()
        bottom.setBackground(new Background(new BackgroundFill(
        Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)))
        bottom.setPadding(new Insets(16))
        bottom.setBorder(blue)
        if(primerPanel){
            bottom.add(this.labelNombre1, 1, 0)
            bottom.add(this.labelPuntPropia1, 1, 1)
            bottom.add(this.labelPuntHijos1, 1, 2)
            bottom.add(this.labelMenciones1, 1, 3)
        }
        else{
            bottom.add(this.labelNombre2, 1, 0)
            bottom.add(this.labelPuntPropia2, 1, 1)
            bottom.add(this.labelPuntHijos2, 1, 2)
            bottom.add(this.labelMenciones2, 1, 3)
        }
        bottom.add(createLabel("Nombre: ", Color.BLACK, 24),0,0)
        bottom.add(createLabel("Puntuacion propia: ", Color.BLACK, 24),0,1)
        bottom.add(createLabel("Puntuacion hijos: ", Color.BLACK, 24),0,2)
        bottom.add(createLabel("Menciones: ", Color.BLACK, 24),0,3)
        return bottom
    }
    
    def rigthView(treeController : TreeController, chartCreator : ChartCreator) : GridPane = {
      var right = new GridPane()
        right.setBackground(new Background(new BackgroundFill(
        Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)))
        right.setPadding(new Insets(16))
        right.setBorder(blue)
        right.add(createLabel("X axis: ", Color.BLACK, 24),0,0)
        var options1 = FXCollections.observableArrayList(
            "Subjerarquias",
            "Tiempo"
        )
        var comboBox1 = new ComboBox(options1)
        comboBox1.valueProperty().addListener(new ChangeListener[String]() {
          override def changed(ov : ObservableValue[_ <: String] ,viejo : String , nuevo : String) {
              System.out.println(viejo)
              System.out.println(nuevo)
              chartCreator.setXAxis(nuevo)
              treeController.updateChart( if(nuevo=="Subjerarquias") chartCreator.getBarChartFromId() else chartCreator.getLineChartFromId())
          }    
        })
        right.add(comboBox1, 0, 1)
        right.add(createLabel("Y axis: ", Color.BLACK, 24),0,2)
        var options2 = FXCollections.observableArrayList(
            "Resumen",
            "Menciones",
            "Puntuacion propia",
            "Puntuacion hijos"
        )
        var comboBox2 = new ComboBox(options2)
        comboBox1.getSelectionModel().selectFirst()
        comboBox2.getSelectionModel().selectFirst()
        comboBox2.valueProperty().addListener(new ChangeListener[String]() {
          override def changed(ov : ObservableValue[_ <: String] ,viejo : String , nuevo : String) {
              System.out.println(viejo)
              System.out.println(nuevo)
              chartCreator.setYAxis(nuevo)
              treeController.updateChart( if(comboBox1.getSelectionModel.getSelectedItem=="Subjerarquias") chartCreator.getBarChartFromId() else chartCreator.getLineChartFromId())
          }    
        })
        right.add(comboBox2, 0, 3)
        return right
    }
    
  def muestraError(mensaje : String) = {
    var alert = new Alert(AlertType.INFORMATION)
    alert.setTitle("Error Dialog")
    alert.setHeaderText(null)
    alert.setContentText(mensaje)
    alert.showAndWait()
  }
    

}