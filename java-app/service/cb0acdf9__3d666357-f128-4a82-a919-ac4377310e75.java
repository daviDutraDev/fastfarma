package service;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class NotificacaoService {


    private static int quantidadeNotificacoes = 0;



    public static void resetarContador(){

        quantidadeNotificacoes = 0;

    }



    public static void mostrarNotificacao(
            String titulo,
            String mensagem
    ) {


        JWindow janela = new JWindow();


        JPanel painel = new JPanel();


        painel.setLayout(
                new BorderLayout()
        );


        painel.setBackground(
                new Color(40,40,40)
        );


        painel.setBorder(
                new EmptyBorder(15,20,15,20)
        );



        JLabel tituloLabel =
                new JLabel(titulo);


        tituloLabel.setForeground(
                Color.ORANGE
        );


        tituloLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        18
                )
        );



        JLabel mensagemLabel =
                new JLabel(mensagem);



        mensagemLabel.setForeground(
                Color.WHITE
        );



        mensagemLabel.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        15
                )
        );



        painel.add(
                tituloLabel,
                BorderLayout.NORTH
        );


        painel.add(
                mensagemLabel,
                BorderLayout.CENTER
        );


        janela.add(painel);


        janela.pack();



        Dimension tela =
                Toolkit.getDefaultToolkit()
                        .getScreenSize();



        int x =
                tela.width
                        - janela.getWidth()
                        - 20;



        int y =
                tela.height
                        - janela.getHeight()
                        - 50
                        - (quantidadeNotificacoes * 100);



        quantidadeNotificacoes++;



        janela.setLocation(
                x,
                y
        );



        janela.setAlwaysOnTop(true);


        janela.setVisible(true);



        Timer timer =
                new Timer(
                        10000,
                        e -> {


                            janela.dispose();



                            if(quantidadeNotificacoes > 0){

                                quantidadeNotificacoes--;

                            }


                        }
                );



        timer.setRepeats(false);


        timer.start();

    }
}