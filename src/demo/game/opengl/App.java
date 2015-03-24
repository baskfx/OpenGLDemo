package demo.game.opengl;

public class App {
	
	public float Alpha;
	public float Beta;
	public float Gamma;
	
	private static App instance;
	private App(){}
	
	public static App Instance()
	{
		if(instance == null)
			instance = new App();
		return instance;
	}

}
