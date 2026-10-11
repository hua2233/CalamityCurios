using System.IO;
using System.Linq;
using Microsoft.Xna.Framework.Graphics;
using Terraria;
using Terraria.ModLoader;

namespace test
{
	public class MinecraftCalamity : Mod
	{

		//我不会开发泰拉瑞亚模组，至少现在不会。这只是一个纹理提取器.
        public override void PostSetupContent()
        {
			Main.QueueMainThreadAction(() =>{
				string[] get = ["noise", "Perlin_2"];
				foreach(string tex in get)
				{
					string t = tex, path = tex;
					if(tex[^2] == '_')
					{
						t = tex[..^2];
					}

					Texture2D tex2D = Main.Assets.Request<Texture2D>($"Images/Misc/{t}").Value;
					SaveToPng(tex2D, $"D:/MinecraftMod/CalamityCurios-1.20.1/src/main/resources/assets/calamity_curios/textures/effect/{path.ToLower()}.png");
				}
			});

        }

		private static void SaveToPng(Texture2D texture, string path)
		{
            using FileStream fs = File.Open(path, FileMode.Create);
            texture.SaveAsPng(fs, texture.Width, texture.Height);
        }
	}
}