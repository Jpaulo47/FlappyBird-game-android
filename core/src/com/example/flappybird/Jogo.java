package com.example.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Jogo extends ApplicationAdapter {

    private SpriteBatch batch;
    private Texture[] passaros;
    private Texture fundo;
    private Texture canoBaixo;
    private Texture canoTopo;
    private Texture gameOver;

    //formas para colisão
    private ShapeRenderer shapeRenderer;
    private Circle circuloPassaro;
    private Rectangle retanguloCanoCima;
    private Rectangle retanguloCanoBaixo;

    //Atributos de configurações
    private float larguraDispositivo;
    private float alturaDispositivo;
    private float variacao = 0;
    private float gravidade = 2;
    private float posicaoInicialVertical = 0;
    private float posicaoCanoHorizontal;
    private float posicaoCanoVertical;
    private float espacoEntreCanos;
    private Random random;
    private int pontos = 0;
    private int pontuacaoMaxima = 0;
    private boolean passouCano = false;
    private int estadoJogo = 0;
    private float posicaoHorizontalPassaro = 0;

    //Exibição de textos
    BitmapFont textoPontuacao;
    BitmapFont textoReiniciar;
    BitmapFont textoMelhorPontuacao;

    //Configuração dos sons
    Sound somVoando;
    Sound somColisao;
    Sound somPontuacao;

    //Objeto Salvar Pontuação
    Preferences preferences;

    //Objetos para a câmera
    private OrthographicCamera camera;
    private Viewport viewport;
    private final float VIRTUAL_WIDTH = 720;
    private final float VIRTUAL_HEIGHT = 1280;

    @Override
    public void create() {
        inicializarTexturas();
        inicializarObjetos();

    }

    @Override
    public void render() {

        //Limpar frames anteriores
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BITS);

        verificarEstadoJogo();
        validarPontos();
        desenharTexturas();
        detectarColisoes();
    }

    private void verificarEstadoJogo() {

        boolean toqueTela = Gdx.input.justTouched();

        if (estadoJogo == 0) {

            /* aplica evento de clique na tela */
            if (toqueTela) {
                gravidade = -15;
                estadoJogo = 1;
                somVoando.play();
            }

        } else if (estadoJogo == 1) {

            /* aplica evento de clique na tela */
            if (toqueTela) {
                gravidade = -15;
                somVoando.play();
            }

            /*movimentar o cano*/
            posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200;
            if (posicaoCanoHorizontal < -canoBaixo.getWidth()) {
                posicaoCanoHorizontal = larguraDispositivo;
                posicaoCanoVertical = random.nextInt(800) - 400;
                passouCano = false;
            }

            /* Aplicando gravidade no passaro 500 - 2 = 498 */
            if (posicaoInicialVertical > 0 || toqueTela)
                posicaoInicialVertical = posicaoInicialVertical - gravidade;

            gravidade++;
            //gravidade = gravidade + 2;

        } else if (estadoJogo == 2) {

            if ( pontos > pontuacaoMaxima){
                pontuacaoMaxima = pontos;
                preferences.putInteger("pontuacaoMaxima", pontuacaoMaxima);

                preferences.flush();
            }

            posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime() * 500;

            if (toqueTela) {
                estadoJogo = 0;
                pontos = 0;
                gravidade = 0;
                posicaoHorizontalPassaro = 0;
                posicaoInicialVertical = alturaDispositivo / 2;
                posicaoCanoHorizontal = larguraDispositivo;
            }
        }

        /* aplica evento de clique na tela */
        if (toqueTela) {
            gravidade = -13;
        }

    }

    private void detectarColisoes() {

        circuloPassaro.set((float) (50 + posicaoHorizontalPassaro + passaros[0].getWidth() * 0.5), (float) (posicaoInicialVertical + passaros[0].getHeight() * 0.5), (float) (passaros[0].getHeight() * 0.5));
        retanguloCanoBaixo.set(posicaoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical, canoBaixo.getWidth(), canoBaixo.getHeight());
        retanguloCanoCima.set(posicaoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical, canoTopo.getWidth(), canoTopo.getHeight());

        boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
        boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);

        if (colidiuCanoCima || colidiuCanoBaixo) {

            if ( estadoJogo == 1){
                somColisao.play();
                estadoJogo = 2;
            }
        }

        /*
         shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
         shapeRenderer.setColor(Color.RED);


         shapeRenderer.circle((float) (50 + passaros[0].getWidth() * 0.5), (float) (posicaoInicialVertical + passaros[0].getHeight() * 0.5), (float) (passaros[0].getHeight() * 0.5));
         //Cano tpo
         shapeRenderer.rect(posicaoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical, canoTopo.getWidth(), canoTopo.getHeight());
         //cano baixo
         shapeRenderer.rect(posicaoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical, canoBaixo.getWidth(), canoBaixo.getHeight());

         shapeRenderer.end();
        */

    }

    private void desenharTexturas() {

        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);
        batch.draw(passaros[(int) variacao], 50 + posicaoHorizontalPassaro, posicaoInicialVertical);
        batch.draw(canoBaixo, posicaoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical);
        batch.draw(canoTopo, posicaoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical);
        textoPontuacao.draw(batch, String.valueOf(pontos), larguraDispositivo / 2 - 50, alturaDispositivo - 110);

        if (estadoJogo == 2) {
            batch.draw(gameOver, (float) (larguraDispositivo / 2 - gameOver.getWidth() * 0.5), alturaDispositivo / 2);
            textoReiniciar.draw(batch, "Toque para reiniciar!", larguraDispositivo / 2 - 140, (float) (alturaDispositivo / 2 - gameOver.getHeight() * 0.5));
            textoMelhorPontuacao.draw(batch, "Ser record é: " + pontuacaoMaxima +  "  pontos", larguraDispositivo / 2 - 140, alturaDispositivo / 2 - gameOver.getHeight());
        }

        batch.end();

    }

    private void validarPontos() {

        if (posicaoCanoHorizontal < 50 - passaros[0].getWidth()) {//passou do passaro
            if (!passouCano) {
                pontos++;
                passouCano = true;
                somPontuacao.play();
            }

        }

        variacao += Gdx.graphics.getDeltaTime() * 10;
        /*verifica variação para bater asas do passaro*/
        if (variacao > 3) variacao = 0;
    }

    private void inicializarTexturas() {

        passaros = new Texture[3];
        passaros[0] = new Texture("passaro1.png");
        passaros[1] = new Texture("passaro2.png");
        passaros[2] = new Texture("passaro3.png");

        fundo = new Texture("fundo.png");
        canoBaixo = new Texture("cano_baixo_maior.png");
        canoTopo = new Texture("cano_topo_maior.png");
        gameOver = new Texture("game_over.png");

    }

    private void inicializarObjetos() {

        batch = new SpriteBatch();
        random = new Random();

        larguraDispositivo = VIRTUAL_WIDTH;
        alturaDispositivo = VIRTUAL_HEIGHT;
        posicaoInicialVertical = alturaDispositivo / 2;
        posicaoCanoHorizontal = larguraDispositivo;
        espacoEntreCanos = 200;

        //Configurações dos textos
        textoPontuacao = new BitmapFont();
        textoPontuacao.setColor(Color.WHITE);
        textoPontuacao.getData().setScale(10);

        textoReiniciar = new BitmapFont();
        textoReiniciar.setColor(Color.BLACK);
        textoReiniciar.getData().setScale(2);

        textoMelhorPontuacao = new BitmapFont();
        textoMelhorPontuacao.setColor(Color.RED);
        textoMelhorPontuacao.getData().setScale(2);

        //Formas geometricas para colisoes/
        circuloPassaro = new Circle();
        retanguloCanoBaixo = new Rectangle();
        retanguloCanoCima = new Rectangle();
        shapeRenderer = new ShapeRenderer();

        //Inicializar sons
        somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
        somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
        somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

        //Configuração de preferencia dos objetos
        preferences = Gdx.app.getPreferences("flappyBird");
        pontuacaoMaxima = preferences.getInteger("pontuacaoMaxima", 0);

        //Configuração da câmera
        camera = new OrthographicCamera();
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        Gdx.app.log("dispose", "descarte de conteudos");
    }
}
