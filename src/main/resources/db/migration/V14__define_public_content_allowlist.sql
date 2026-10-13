-- Public content is opt-in. Locale variants follow the
-- <content-key>.<locale> convention and are filtered by the application
-- allowlist in SettingDefinitionRegistry.
UPDATE public_configs
SET scope = 'INTERNAL',
    active = FALSE
WHERE scope = 'PUBLIC'
  AND config_key NOT IN (
      'zalo_link',
      'contact.phone',
      'contact.email',
      'contact.address',
      'footer.description',
      'footer.description.vi',
      'footer.description.en',
      'footer.contact_url',
      'footer.returns_url',
      'footer.tracking_url',
      'footer.size_guide_url',
      'footer.faq_url',
      'footer.facebook_url',
      'footer.instagram_url',
      'seo.home.title.vi',
      'seo.home.title.en',
      'seo.home.description.vi',
      'seo.home.description.en',
      'seo.products.title.vi',
      'seo.products.title.en',
      'seo.products.description.vi',
      'seo.products.description.en',
      'banner.home.image_url',
      'banner.home.link'
  );

UPDATE public_configs
SET scope = 'PUBLIC',
    active = TRUE
WHERE config_key IN (
      'zalo_link',
      'contact.phone',
      'contact.email',
      'contact.address',
      'footer.description',
      'footer.description.vi',
      'footer.description.en',
      'footer.contact_url',
      'footer.returns_url',
      'footer.tracking_url',
      'footer.size_guide_url',
      'footer.faq_url',
      'footer.facebook_url',
      'footer.instagram_url',
      'seo.home.title.vi',
      'seo.home.title.en',
      'seo.home.description.vi',
      'seo.home.description.en',
      'seo.products.title.vi',
      'seo.products.title.en',
      'seo.products.description.vi',
      'seo.products.description.en',
      'banner.home.image_url',
      'banner.home.link'
  );
