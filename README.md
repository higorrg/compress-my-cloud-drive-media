# compress-my-cloud-drive-media

Initially the goal was to compress only my video files from Google Drive, but as things get working, images and PDF files also was included.

The application architecture is based on mix of chain of responsability [(1)](https://sourcemaking.com/design_patterns/chain_of_responsibility) [(2)](https://refactoring.guru/design-patterns/chain-of-responsibility) and Observer [(3)](https://sourcemaking.com/design_patterns/observer) [(4)](https://refactoring.guru/design-patterns/observer).

This architecture enables the implementation of any compressor interacting with any cloud drive, by just implementing interfaces and registration in the factory [(5)](https://refactoring.guru/design-patterns/factory-method) [(6)](https://sourcemaking.com/design_patterns/abstract_factory).

Here is the representation pictore:
