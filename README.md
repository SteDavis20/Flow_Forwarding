# Flow_Forwarding

Assignment #2 of Computer Networks module.

The basic functionality that a forwarding service has to provide is to:
	
	accept incoming packets, 
	inspect the header information, 
	consult the forwarding table and
	forward the header and payload information to the destination.

In the first place, if a destination is not known, a forwarding service would drop an incoming packet.

Once a controller has been implemented, the forwarding service needs to:

	contact the controller to enquire about the relevant forwarding information to the unknown destination
	and if it receives forwarding information, integrate this into its forwarding table.

The network elements should be controlled by a central controller.
The central controller initializes the flow tables of the network elements.
The central controller informs the network elements about routes to destinations (via the flow table) as the network changes.

In a first step, in order to reduce complexity, the flow tables of the individual network elements should be hardcoded.

The implementation of the controller element should only be pursued once:
	the implementation of the forwarding functionality of the network elements is stable and allows for basic communication between two endpoints.