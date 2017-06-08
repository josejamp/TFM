package main.scala.gui

import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.beans.value.ObservableValue
import javafx.beans.value.ChangeListener
import javafx.scene.input.MouseEvent
import javafx.event.EventHandler

import main.scala.Jerarquia
import main.scala.dataBase.TraduccionDatos
import main.scala.gui.controller.TreeController

class TreeCreator(controller : TreeController) {
  
  var treeController = controller
  
  def setController(controller : TreeController) = {
    this.treeController = controller
  }
  
  def createTree(chartCreator : ChartCreator) : TreeView[TreeItemPair] = {
    var tree = new TreeView[TreeItemPair] (TreeCreator.createTreeItems(controller.getJerarquia));
    tree.getSelectionModel().selectedItemProperty().addListener( new ChangeListener[TreeItem[TreeItemPair]]() {

        override def changed(observable : ObservableValue[ _ <: TreeItem[TreeItemPair]] , oldValue : TreeItem[TreeItemPair], newValue : TreeItem[TreeItemPair] ) : Unit = {

            var selectedItem = newValue.asInstanceOf[TreeItem[TreeItemPair]]
            val (n,pp,ph,m) = treeController.getDataByID(selectedItem.getValue.getIdentificador)
            treeController.setLabels(n, pp, ph, m)
        }

    });
    tree.setOnMouseClicked(new EventHandler[MouseEvent](){
      override def handle(mouseEvent : MouseEvent ) = {            
          if(mouseEvent.getClickCount() == 2) {
              var item = tree.getSelectionModel().getSelectedItem()
              treeController.elementDoubleClicked(item.getValue.getIdentificador, chartCreator)
          }
      }
    });
    
    return tree        
  }
  
}

object TreeCreator {
  
  
  def createTreeItems(jer : Jerarquia) : TreeItem[TreeItemPair] = {
    var rootItem = new TreeItem[TreeItemPair] (new TreeItemPair("Jerarquia","principal"))
    rootItem.setExpanded(true)
    for ( (k,v)<-jer.getNivel ) {
        var item = new TreeItem[TreeItemPair] (new TreeItemPair(k,v._5))
        createTreeItemsAux(v._2, item)
        rootItem.getChildren().add(item)
    }
    return rootItem
  }
  
  def createTreeItemsAux(jer : Jerarquia, father : TreeItem[TreeItemPair]) : Unit = {
    father.setExpanded(false)
    for ( (k,v)<-jer.getNivel ) {
        var item = new TreeItem[TreeItemPair] (new TreeItemPair(k,v._5))
        createTreeItemsAux(v._2, item)
        father.getChildren().add(item)
    }
  }
  
}