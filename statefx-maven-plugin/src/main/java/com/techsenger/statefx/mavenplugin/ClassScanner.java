/*
 * Copyright (c) 2026 Pavel Castornii. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation. This particular file is
 * subject to the "Classpath" exception as provided in the LICENSE file
 * that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.techsenger.statefx.mavenplugin;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyFloatProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.embed.swing.SwingNode;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.DirectionalLight;
import javafx.scene.Group;
import javafx.scene.LightBase;
import javafx.scene.Node;
import javafx.scene.ParallelCamera;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SpotLight;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.Axis;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.BubbleChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Cell;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.Pagination;
import javafx.scene.control.PasswordField;
import javafx.scene.control.PopupControl;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.ChoiceBoxListCell;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.ChoiceBoxTreeCell;
import javafx.scene.control.cell.ChoiceBoxTreeTableCell;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.ComboBoxTreeCell;
import javafx.scene.control.cell.ComboBoxTreeTableCell;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.ProgressBarTreeTableCell;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Box;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;

/**
 *
 * @author Pavel Castornii
 */
final class ClassScanner {

    /**
     * From
     * https://download.java.net/java/GA/javafx25/docs/api/javafx.graphics/javafx/css/Styleable.html ,
     * https://download.java.net/java/GA/javafx25/docs/api/javafx.controls/javafx/scene/control/SelectionModel.html .
     */
    private static final List<Class<?>>  classes = List.of(
            Accordion.class, AmbientLight.class, AnchorPane.class, Arc.class,
            AreaChart.class, Axis.class, BarChart.class, BorderPane.class,
            Box.class, BubbleChart.class, Button.class, ButtonBar.class,
            ButtonBase.class, Camera.class, Canvas.class, CategoryAxis.class,
            Cell.class, Chart.class, CheckBox.class, CheckBoxListCell.class,
            CheckBoxTableCell.class, CheckBoxTreeCell.class, CheckBoxTreeTableCell.class,
            CheckMenuItem.class, ChoiceBox.class, ChoiceBoxListCell.class,
            ChoiceBoxTableCell.class, ChoiceBoxTreeCell.class, ChoiceBoxTreeTableCell.class,
            Circle.class, /* CodeArea.class, */ ColorPicker.class, ComboBox.class,
            ComboBoxBase.class, ComboBoxListCell.class, ComboBoxTableCell.class,
            ComboBoxTreeCell.class, ComboBoxTreeTableCell.class, ContextMenu.class,
            Control.class, CubicCurve.class, CustomMenuItem.class, Cylinder.class,
            DateCell.class, DatePicker.class, DialogPane.class, DirectionalLight.class,
            Ellipse.class, FlowPane.class, GridPane.class, Group.class, HBox.class,
            /* HeaderBar.class , */ HTMLEditor.class, Hyperlink.class, ImageView.class,
            IndexedCell.class, Label.class, Labeled.class, LightBase.class, Line.class,
            LineChart.class, ListCell.class, ListView.class, MediaView.class, Menu.class,
            MenuBar.class, MenuButton.class, MenuItem.class, MeshView.class,
            NestedTableColumnHeader.class, Node.class, NumberAxis.class, Pagination.class,
            Pane.class, ParallelCamera.class, Parent.class, PasswordField.class,
            Path.class, PerspectiveCamera.class, PieChart.class, PointLight.class,
            Polygon.class, Polyline.class, PopupControl.class,
            ProgressBar.class, ProgressBarTableCell.class, ProgressBarTreeTableCell.class,
            ProgressIndicator.class, QuadCurve.class, RadioButton.class, RadioMenuItem.class,
            Rectangle.class, Region.class, /* RichTextArea.class , */ ScatterChart.class,
            ScrollBar.class, ScrollPane.class, Separator.class, SeparatorMenuItem.class,
            Shape.class, Shape3D.class, Slider.class, Sphere.class, Spinner.class,
            SplitMenuButton.class, SplitPane.class, SpotLight.class, StackedAreaChart.class,
            StackedBarChart.class, StackPane.class, SubScene.class, SVGPath.class,
            SwingNode.class, Tab.class, TableCell.class, TableColumn.class,
            TableColumnBase.class, TableColumnHeader.class, TableHeaderRow.class,
            TableRow.class, TableView.class, TabPane.class, Text.class, TextArea.class,
            TextField.class, TextFieldListCell.class, TextFieldTableCell.class,
            TextFieldTreeCell.class, TextFieldTreeTableCell.class, TextFlow.class,
            TextInputControl.class, TilePane.class, TitledPane.class, ToggleButton.class,
            ToolBar.class, Tooltip.class, TreeCell.class, TreeTableCell.class,
            TreeTableColumn.class, TreeTableRow.class, TreeTableView.class, TreeView.class,
            ValueAxis.class, VBox.class, VirtualFlow.class, WebView.class, XYChart.class,

            SingleSelectionModel.class, MultipleSelectionModel.class);

    /**
     * Recursively builds class hierarchy (from base to given class, excluding Object).
     */
    private static List<Class<?>> getClassHierarchy(Class<?> clazz) {
        List<Class<?>> hierarchy = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null && current != Object.class) {
            hierarchy.add(current);
            current = current.getSuperclass();
        }

        Collections.reverse(hierarchy);
        return hierarchy;
    }

    static void setupProperty(PropertyMeta property) {
        if (ReadOnlyBooleanProperty.class.isAssignableFrom(property.getRawType())) {
            property.setReadOnlyType(ReadOnlyBooleanProperty.class);
            property.setType(BooleanProperty.class);
            property.setValueType("boolean");
            property.setGetterPrefix("is");
            property.setGenericParameter("");
            property.setWrapperType("Boolean");
        } else if (ReadOnlyStringProperty.class.isAssignableFrom(property.getRawType())) {
            property.setReadOnlyType(ReadOnlyStringProperty.class);
            property.setType(StringProperty.class);
            property.setValueType("String");
            property.setGetterPrefix("get");
            property.setGenericParameter("");
            property.setWrapperType("String");
        } else if (ReadOnlyIntegerProperty.class.isAssignableFrom(property.getRawType())) {
            property.setReadOnlyType(ReadOnlyIntegerProperty.class);
            property.setType(IntegerProperty.class);
            property.setValueType("int");
            property.setGetterPrefix("get");
            property.setGenericParameter("");
            property.setWrapperType("Integer");
        } else if (ReadOnlyLongProperty.class.isAssignableFrom(property.getRawType())) {
            property.setReadOnlyType(ReadOnlyLongProperty.class);
            property.setType(LongProperty.class);
            property.setValueType("long");
            property.setGetterPrefix("get");
            property.setGenericParameter("");
            property.setWrapperType("Long");
        } else if (ReadOnlyDoubleProperty.class.isAssignableFrom(property.getRawType())) {
            property.setReadOnlyType(ReadOnlyDoubleProperty.class);
            property.setType(DoubleProperty.class);
            property.setValueType("double");
            property.setGetterPrefix("get");
            property.setGenericParameter("");
            property.setWrapperType("Double");
        } else if (ReadOnlyFloatProperty.class.isAssignableFrom(property.getRawType())) {
            property.setReadOnlyType(ReadOnlyFloatProperty.class);
            property.setType(FloatProperty.class);
            property.setValueType("float");
            property.setGetterPrefix("get");
            property.setGenericParameter("");
            property.setWrapperType("Float");
        } else if (ReadOnlyObjectProperty.class.isAssignableFrom(property.getRawType())) {
            property.setReadOnlyType(ReadOnlyObjectProperty.class);
            property.setType(ObjectProperty.class);
            property.setValueType("T");
            property.setGetterPrefix("get");
            property.setGenericParameter("<T>");
            property.setWrapperType("Object");
        }
    }

    private final Set<PropertyMeta> properties = new HashSet<>();

    private final Set<ContainerMeta> collections = new HashSet<>();

    private final Set<ContainerMeta> maps = new HashSet<>();

    ClassScanner() {

    }

    public void scan() {
        Set<Class<?>> allClasses = new HashSet<>();
        classes.stream().map(c -> getClassHierarchy(c)).forEach(l -> allClasses.addAll(l));
        Set<PropertyMeta> allProperties = new HashSet<>();
        for (var c : allClasses) {
            scan(c);
        }
    }

    public Set<PropertyMeta> getProperties() {
        return properties;
    }

    public Set<ContainerMeta> getCollections() {
        return collections;
    }

    public Set<ContainerMeta> getMaps() {
        return maps;
    }

    private void scan(Class<?> clazz) {
        for (var method : clazz.getMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                var propStr = "Property";
                var index = method.getName().lastIndexOf(propStr);
                if (index > 0 && (method.getName().length() == index + propStr.length())) {
                    var name = method.getName().substring(0, index);
                    var prop = new PropertyMeta(clazz, method.getReturnType(), name);
                    setupProperty(prop);
                    properties.add(prop);
                }
                if (!method.getName().startsWith("get")) {
                    continue;
                }
                Class<?> type = null;
                if (ObservableList.class.isAssignableFrom(method.getReturnType())) {
                    type = ObservableList.class;
                } else if (ObservableSet.class.isAssignableFrom(method.getReturnType())) {
                    type = ObservableSet.class;
                } else if (ObservableMap.class.isAssignableFrom(method.getReturnType())) {
                    type = ObservableMap.class;
                }

                if (type != null) {
                    var capitalizedName = method.getName().substring(3);
                    if (type == ObservableMap.class) {
                        var map = new ContainerMeta(clazz, method.getReturnType(), capitalizedName);
                        map.setType(type);
                        this.maps.add(map);
                    } else {
                        var col = new ContainerMeta(clazz, method.getReturnType(), capitalizedName);
                        col.setType(type);
                        this.collections.add(col);
                    }
                }
            }
        }
    }
}
