import React, { Component } from 'react';
import WelcomePageContent from './welcome.component';
import { withWebId } from '@inrupt/solid-react-components';
import data from '@solid/query-ldflex';
import { withToastManager } from 'react-toast-notifications';

const defaultProfilePhoto = '/img/icon/empty-profile.svg';

/**
 * Container component for the Welcome Page, containing example of how to fetch data from a POD
 */
class WelcomeComponent extends Component<Props> {
  constructor(props) {
    super(props);

    this.state = {
      name: '',
      image: defaultProfilePhoto,
      isLoading: false,
      hasImage: false,
      hits:[]
    };
  }
  
  componentDidMount() {
    if (this.props.webId) {
      this.getProfileData();
    }
  }//componentDidMount()


  componentDidUpdate(prevProps, prevState) {
    if (this.props.webId && this.props.webId !== prevProps.webId) {
      this.getProfileData();
    }
  }

  /**
   * This function retrieves a user's card data and tries to grab both the user's name and profile photo if they exist.
   *
   * This is an example of how to use the LDFlex library to fetch different linked data fields.
   */
  getProfileData = async () => {
    this.setState({ isLoading: true });
    let hasImage;

    /*
     * This is an example of how to use LDFlex. Here, we're loading the webID link into a user variable. This user object
     * will contain all of the data stored in the webID link, such as profile information. Then, we're grabbing the user.name property
     * from the returned user object.
     */
    const user = data[this.props.webId];
    const nameLd = await user.name;

    const name = nameLd ? nameLd.value : '';

    let imageLd = await user.image;
    imageLd = imageLd ? imageLd : await user.vcard_hasPhoto;

    let image;
    if (imageLd && imageLd.value) {
      image = imageLd.value;
      hasImage = true;
    } else {
      hasImage = false;
      image = defaultProfilePhoto;
    }

    this.setState({ name, image, isLoading: false, hasImage });
  };

  /**
   * updatedPhoto will update the photo url on vcard file
   * this function will check if user has image or hasPhoto node if not
   * will just update it, the idea is use image instead of hasPhoto
   * @params{String} uri photo url
   */
  updatePhoto = async (uri: String, message) => {
    try {
      const { user } = data;
      this.state.hasImage
        ? await user.image.set(uri)
        : await user.image.add(uri);

      this.props.toastManager.add(['', message], {
        appearance: 'success'
      });
    } catch (error) {
      this.props.toastManager.add(['Error', error.message], {
        appearance: 'error'
      });
    }
  };

  render() {
    const { name, image, isLoading,results,driveTime,weather,weatherPenalty } = this.state;

    return (
      <WelcomePageContent
        name={name}
        image={image}
        isLoading={isLoading}
        webId={this.props.webId}
        updatePhoto={this.updatePhoto}
        results={results}
        driveTime={driveTime}
        weather={weather}
        weatherPenalty={weatherPenalty}
        sliderVal={0}
      />
    );
  }
}

export default withWebId(withToastManager(WelcomeComponent));
