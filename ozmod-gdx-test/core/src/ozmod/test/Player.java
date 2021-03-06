package ozmod.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Player extends ApplicationAdapter {
	
	public static final String testfile ="pms_hypr.it";
	
	SpriteBatch batch;
	Texture img;
	ModMusicPlayer modMusicPlayer;

	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
		modMusicPlayer = new ModMusicPlayer();
		modMusicPlayer.loadUsingPlist();
		modMusicPlayer.play(1f);
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}
	
	@Override
	public void pause() {
		super.pause();
		modMusicPlayer.pause();
	}
	
	@Override
	public void resume() {
		super.resume();
		modMusicPlayer.resume();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();
	}
}
