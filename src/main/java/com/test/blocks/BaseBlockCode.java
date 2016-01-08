//This is the package (folder) your blocks are in. Instead of slashes (/) programmers use "."
package com.test.blocks;
 
//This is where you import all the things you will need in your program.
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

//Wherever it says "BaseBlockCode" replace it with the name of your block, for example "MyBlock"
public class BaseBlockCode extends Block{
	public BaseBlockCode() {
		
//This is the material of your block. For example, you could do "Material.cloth" for a cloth sounding block, or "Material.iron" for an iron sounding block.
		super(Material.rock);
		
//This is the hardness of your block. For example, setting it to "(25.0F)" would make the block harder.
		this.setHardness(15.0F);
	

	}
}	