import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class FunctionBuilder extends JFrame {
    public static void main(String[] args) {
        new FunctionBuilder();
    }

    public FunctionBuilder() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(new GraphComponent(), BorderLayout.CENTER);
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
    }

    public class GraphComponent extends JComponent {
        float unit = 20;                /*Переменная масштабирования*/
        Point startDrag, endDrag;       /*Отслеживание смещения графика при помощи мыши*/
        int xDragged, yDragged = 0;     /*Переменные смещения графика относительно центра координат*/
        int distance = 100;             /*Расстояние между осями*/
        float x,y;                      /*значения в декартовой системе координат*/

        public GraphComponent() {
            super();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setPreferredSize(new Dimension((int)(screenSize.getWidth()*0.7),(int)(screenSize.getHeight()*0.7)));

            //Изменение масштаба
            this.addMouseWheelListener(new MouseWheelListener() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    int steps = e.getWheelRotation();
                    if(steps > 0){
                        unit-=1.*unit/15;
                        distance-=distance/24;
                        if(distance<=60) distance+=100;}
                    if(steps < 0){
                        unit+=1.*unit/15;
                        distance+=distance/24;
                        if(distance >=160) distance-=100;}

                    repaint();
                }
            });

            this.addMouseListener(new MouseAdapter() {

                public void mousePressed(MouseEvent e) {
                    startDrag = new Point(e.getX(), e.getY());
                    endDrag = startDrag;
                }

                public void mouseReleased(MouseEvent e) {
                    startDrag = null;
                    endDrag = null;
                    repaint();
                }
            });

            this.addMouseMotionListener(new MouseMotionAdapter() {

                public void mouseDragged(MouseEvent e) {
                    endDrag = new Point(e.getX(), e.getY());
                    xDragged+=endDrag.x-startDrag.x;
                    yDragged+=endDrag.y-startDrag.y;
                    repaint();
                    startDrag = new Point(e.getX(), e.getY());
                    endDrag = startDrag;
                }

            });
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setBackground(Color.DARK_GRAY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int width = super.getWidth();
            int height = super.getHeight();

            //Очищаем область
            g2d.clearRect(0, 0, width, height);
            g2d.setStroke(new BasicStroke(2));
            g2d.setColor(Color.BLACK);
            g2d.drawLine(width / 2+xDragged, 0, width / 2+xDragged, height);
            g2d.drawLine(0, height / 2+yDragged, width, height / 2+yDragged);
            g2d.setStroke(new BasicStroke(1));

            //Отрисовка вертикальных осей
            for (int i = width / 2 + distance; i < width-xDragged; i += distance)
                g2d.drawLine(i+xDragged, 0, i+xDragged, height);
            for (int i = width / 2 - distance; i > 0-xDragged; i -= distance)
                g2d.drawLine(i+xDragged, 0, i+xDragged, height);

            //Отрисовка горизонтальных осей
            for (int i = height / 2 + distance; i < height-yDragged; i += distance)
                g2d.drawLine(0, i+yDragged, width, i+yDragged);
            for (int i = height / 2 - distance; i > 0-yDragged; i -= distance)
                g2d.drawLine(0, i+yDragged, width, i+yDragged);

            //Отрисовка графика функции
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2));
            DrawFunction(g2d, width, height);

            //Нижний ряд чисел
            float d;
            BigDecimal bd;
            g2d.setColor(Color.WHITE);
            for(int i=width/2; i<width-xDragged; i+= distance){
                d = (i-width/2)/unit;
                bd=new BigDecimal(d).setScale(3, RoundingMode.HALF_EVEN);
                g2d.drawString(bd+"", i+5+xDragged, height-5);}
            for(int i = width/2- distance; i>0-xDragged; i-= distance){
                d = (i-width/2)/unit;
                bd=new BigDecimal(d).setScale(3, RoundingMode.HALF_EVEN);
                g2d.drawString(bd+"", i+5+xDragged, height-5);}

            //Боковой ряд чисел
            for(int i=height/2; i<height-yDragged; i+= distance){
                d = -1*(i-height/2)/unit;
                bd=new BigDecimal(d).setScale(3, RoundingMode.HALF_EVEN);
                g2d.drawString(bd+"", 5, i-5+yDragged);}
            for(int i = height/2- distance; i>0-yDragged; i-= distance){
                d = -1*(i-height/2)/unit;
                bd=new BigDecimal(d).setScale(3, RoundingMode.HALF_EVEN);
                g2d.drawString(bd+"", 5, i-5+yDragged);}


        }

        private void DrawFunction(Graphics2D g2d, int width, int height){
            Point prev = null;
            final double POSITIVE_INFINITY =1.0/0.0;
            final double NEGATIVE_INFINITY = -1.0/0.0;
            /* xp,yp - значения переменных пиксельной координатной системы,
             * соответсвующие реальным значениям x и y  */

            for(int xp = 0, yp = 0; xp<width;xp++){
                x = (xp-xDragged-width/2)/unit;
                y = (float)(Math.cos(x));
                if(y == POSITIVE_INFINITY) {prev = null; continue;}
                if(y == NEGATIVE_INFINITY) {prev = null; continue;}
                yp = (int)(height/2+yDragged-y*unit);
                if(prev != null)
                    g2d.drawLine(prev.x,prev.y,xp,yp);
                prev = new Point(xp,yp);

            }
        }

    }

}