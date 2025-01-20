# compress-my-cloud-drive-media

Initially the goal was to compress only my video files from Google Drive, but as things get working, images and PDF files also was included.

The application architecture is based on mix of chain of responsability [(1)](https://sourcemaking.com/design_patterns/chain_of_responsibility) [(2)](https://refactoring.guru/design-patterns/chain-of-responsibility) and Observer [(3)](https://sourcemaking.com/design_patterns/observer) [(4)](https://refactoring.guru/design-patterns/observer). Its a mix because initially this could be made just with chain of responsability handlers, but with handlers also been observers, enables the paginated query of cloud client API. Otherwise, it would be necessary to run all files first, and then compress them, diminishing the user experience. 

This architecture enables the implementation of any compressor interacting with any cloud drive, by just implementing interfaces and registering observer-handlers in the factory [(5)](https://refactoring.guru/design-patterns/factory-method) [(6)](https://sourcemaking.com/design_patterns/abstract_factory).

Here is the representation pictore:
