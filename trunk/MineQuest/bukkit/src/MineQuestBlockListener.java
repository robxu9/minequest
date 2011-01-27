

import org.bukkit.event.block.BlockListener;

public class MineQuestBlockListener extends BlockListener {
	
	@Override
	public void onBlockDamage(org.bukkit.event.block.BlockDamageEvent event) {
		Town town = MineQuest.getTown(event.getPlayer());
		Quester quester = MineQuest.getQuester(event.getPlayer());
	
		quester.checkItemInHand();
		if (quester.checkItemInHandAbil()) {
			quester.callAbilityL(event.getBlock());
		}
		quester.destroyBlock(event.getBlock());
		
		if (town != null) {
			Property prop = town.getProperty(event.getPlayer());
			
			if (prop != null) {
				if (prop.canEdit(quester)) {
					return;
				} else {
					event.getPlayer().sendMessage("You are not authorized to modify this property - please get the proper authorization");
					quester.dropRep(20);
					event.setCancelled(true);
					return;
				}
			} else {
				prop = town.getTownProperty();
				if (prop.canEdit(quester)) {
					return;
				} else {
					event.getPlayer().sendMessage("You are not authorized to modify town - please get the proper authorization");
					quester.dropRep(10);
					event.setCancelled(true);
					return;
				}
			}	
		}
		
		super.onBlockDamage(event);
	}
	
	@Override
	public void onBlockRightClick(org.bukkit.event.block.BlockRightClickEvent event) {
		Quester quester = MineQuest.getQuester(event.getPlayer());
		
		quester.checkItemInHand();
		if (quester.checkItemInHandAbil()) {
			quester.callAbilityR(event.getBlock());
		}
		
		super.onBlockRightClick(event);
	}
	
	@Override
	public void onBlockPlace(org.bukkit.event.block.BlockPlaceEvent event) {
		Town town = MineQuest.getTown(event.getPlayer());
		Quester quester = MineQuest.getQuester(event.getPlayer());
		
		quester.checkItemInHand();
		if (quester.checkItemInHandAbil()) {
			quester.callAbilityR(event.getBlock());
		}
		
		if (town != null) {
			Property prop = town.getProperty(event.getPlayer());
			
			if (prop != null) {
				if (prop.canEdit(MineQuest.getQuester(event.getPlayer()))) {
					return;
				} else {
					event.getPlayer().sendMessage("You are not authorized to modify this property - please get the proper authorization");
					MineQuest.getQuester(event.getPlayer()).dropRep(20);
					event.setCancelled(true);
					return;
				}
			} else {
				prop = town.getTownProperty();
				if (prop.canEdit(MineQuest.getQuester(event.getPlayer()))) {
					return;
				} else {
					event.getPlayer().sendMessage("You are not authorized to modify town - please get the proper authorization");
					MineQuest.getQuester(event.getPlayer()).dropRep(10);
					event.setCancelled(true);
					return;
				}
			}
			
		}
		
		super.onBlockPlace(event);
	}

}